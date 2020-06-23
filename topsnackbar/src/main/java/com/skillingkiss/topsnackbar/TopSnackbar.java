/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.skillingkiss.topsnackbar;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.StringRes;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.drawable.DrawableCompat;


import java.lang.reflect.Field;

import static android.view.accessibility.AccessibilityManager.FLAG_CONTENT_CONTROLS;
import static android.view.accessibility.AccessibilityManager.FLAG_CONTENT_ICONS;
import static android.view.accessibility.AccessibilityManager.FLAG_CONTENT_TEXT;
import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * Snackbars provide lightweight feedback about an operation. They show a brief message at the
 * bottom of the screen on mobile and lower left on larger devices. Snackbars appear above all other
 * elements on screen and only one can be displayed at a time.
 *
 * <p>They automatically disappear after a timeout or after user interaction elsewhere on the
 * screen, particularly after interactions that summon a new surface or activity. Snackbars can be
 * swiped off screen.
 *
 * <p>Snackbars can contain an action which is set via {@link #setAction(CharSequence,
 * View.OnClickListener)}.
 *
 * <p>To be notified when a snackbar has been shown or dismissed, you can provide a {@link Callback}
 * via {@link BaseTransientBottomBar#addCallback(BaseCallback)}.
 */
public class TopSnackbar extends BaseTransientBottomBar<TopSnackbar> {

  @Nullable private final AccessibilityManager accessibilityManager;
  private boolean hasAction;

  private static final int[] SNACKBAR_BUTTON_STYLE_ATTR = new int[] {R.attr.snackbarButtonStyle};

  /**
   * Callback class for {@link TopSnackbar} instances.
   *
   * <p>Note: this class is here to provide backwards-compatible way for apps written before the
   * existence of the base {@link BaseTransientBottomBar} class.
   *
   * @see BaseTransientBottomBar#addCallback(BaseCallback)
   */
  public static class Callback extends BaseCallback<TopSnackbar> {
    /** Indicates that the Snackbar was dismissed via a swipe. */
    public static final int DISMISS_EVENT_SWIPE = BaseCallback.DISMISS_EVENT_SWIPE;
    /** Indicates that the Snackbar was dismissed via an action click. */
    public static final int DISMISS_EVENT_ACTION = BaseCallback.DISMISS_EVENT_ACTION;
    /** Indicates that the Snackbar was dismissed via a timeout. */
    public static final int DISMISS_EVENT_TIMEOUT = BaseCallback.DISMISS_EVENT_TIMEOUT;
    /** Indicates that the Snackbar was dismissed via a call to {@link #dismiss()}. */
    public static final int DISMISS_EVENT_MANUAL = BaseCallback.DISMISS_EVENT_MANUAL;
    /** Indicates that the Snackbar was dismissed from a new Snackbar being shown. */
    public static final int DISMISS_EVENT_CONSECUTIVE = BaseCallback.DISMISS_EVENT_CONSECUTIVE;

    @Override
    public void onShown(TopSnackbar sb) {
      // Stub implementation to make API check happy.
    }

    @Override
    public void onDismissed(TopSnackbar transientBottomBar, @DismissEvent int event) {
      // Stub implementation to make API check happy.
    }
  }

  @Nullable private BaseCallback<TopSnackbar> callback;

  private TopSnackbar(
      @NonNull ViewGroup parent,
      @NonNull View content,
      @NonNull com.google.android.material.snackbar.ContentViewCallback contentViewCallback) {
    super(parent, content, contentViewCallback);
    accessibilityManager =
        (AccessibilityManager) parent.getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
  }

  // TODO: Delete this once custom Robolectric shadows no longer depend on this method being present
  // (and instead properly utilize BaseTransientBottomBar hierarchy).
  @Override
  public void show() {
    super.show();
  }

  // TODO: Delete this once custom Robolectric shadows no longer depend on this method being present
  // (and instead properly utilize BaseTransientBottomBar hierarchy).
  @Override
  public void dismiss() {
    super.dismiss();
  }

  // TODO: Delete this once custom Robolectric shadows no longer depend on this method being present
  // (and instead properly utilize BaseTransientBottomBar hierarchy).
  @Override
  public boolean isShown() {
    return super.isShown();
  }

  /**
   * Make a Snackbar to display a message
   *
   * <p>Snackbar will try and find a parent view to hold Snackbar's view from the value given to
   * {@code view}. Snackbar will walk up the view tree trying to find a suitable parent, which is
   * defined as a {@link CoordinatorLayout} or the window decor's content view, whichever comes
   * first.
   *
   * <p>Having a {@link CoordinatorLayout} in your view hierarchy allows Snackbar to enable certain
   * features, such as swipe-to-dismiss and automatically moving of widgets.
   *
   * @param view The view to find a parent from. This view is also used to find the anchor view when
   *     calling {@link TopSnackbar#setAnchorView(int)}.
   * @param text The text to show. Can be formatted text.
   * @param duration How long to display the message. Can be {@link #LENGTH_SHORT}, {@link
   *     #LENGTH_LONG}, {@link #LENGTH_INDEFINITE}, or a custom duration in milliseconds.
   */
  @NonNull
  public static TopSnackbar make(
      @NonNull View view, @NonNull CharSequence text, @Duration int duration) {
    final ViewGroup parent = findSuitableParent(view);
    if (parent == null) {
      throw new IllegalArgumentException(
          "No suitable parent found from the given view. Please provide a valid view.");
    }

    final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
    final SnackbarContentLayout content =
        (SnackbarContentLayout)
            inflater.inflate(
                hasSnackbarButtonStyleAttr(parent.getContext())
                    ? R.layout.mtrl_layout_topsnackbar_include
                    : R.layout.design_layout_topsnackbar_include,
                parent,
                false);
    final TopSnackbar topSnackbar = new TopSnackbar(parent, content, content);
    topSnackbar.setText(text);
    topSnackbar.setDuration(duration);
    return topSnackbar;
  }

  /**
   * {@link TopSnackbar}s should still work with AppCompat themes, which don't specify a {@code
   * snackbarButtonStyle}. This method helps to check if a valid {@code snackbarButtonStyle} is set
   * within the current context, so that we know whether we can use the attribute.
   */
  protected static boolean hasSnackbarButtonStyleAttr(@NonNull Context context) {
    TypedArray a = context.obtainStyledAttributes(SNACKBAR_BUTTON_STYLE_ATTR);
    int snackbarButtonStyleResId = a.getResourceId(0, -1);
    a.recycle();
    return snackbarButtonStyleResId != -1;
  }

  /**
   * Make a Snackbar to display a message.
   *
   * <p>Snackbar will try and find a parent view to hold Snackbar's view from the value given to
   * {@code view}. Snackbar will walk up the view tree trying to find a suitable parent, which is
   * defined as a {@link CoordinatorLayout} or the window decor's content view, whichever comes
   * first.
   *
   * <p>Having a {@link CoordinatorLayout} in your view hierarchy allows Snackbar to enable certain
   * features, such as swipe-to-dismiss and automatically moving of widgets.
   *
   * @param view The view to find a parent from.
   * @param resId The resource id of the string resource to use. Can be formatted text.
   * @param duration How long to display the message. Can be {@link #LENGTH_SHORT}, {@link
   *     #LENGTH_LONG}, {@link #LENGTH_INDEFINITE}, or a custom duration in milliseconds.
   */
  @NonNull
  public static TopSnackbar make(@NonNull View view, @StringRes int resId, @Duration int duration) {
    return make(view, view.getResources().getText(resId), duration);
  }

  @Nullable
  private static ViewGroup findSuitableParent(View view) {
    ViewGroup fallback = null;
    do {
      if (view instanceof CoordinatorLayout) {
        // We've found a CoordinatorLayout, use it
        return (ViewGroup) view;
      } else if (view instanceof FrameLayout) {
        if (view.getId() == android.R.id.content) {
          // If we've hit the decor content view, then we didn't find a CoL in the
          // hierarchy, so use it.
          return (ViewGroup) view;
        } else {
          // It's not the content view but we'll use it as our fallback
          fallback = (ViewGroup) view;
        }
      }

      if (view != null) {
        // Else, we will loop and crawl up the view hierarchy and try to find a parent
        final ViewParent parent = view.getParent();
        view = parent instanceof View ? (View) parent : null;
      }
    } while (view != null);

    // If we reach here then we didn't find a CoL or a suitable content view so we'll fallback
    return fallback;
  }

  /**
   * Update the text in this {@link TopSnackbar}.
   *
   * @param message The new text for this {@link BaseTransientBottomBar}.
   */
  @NonNull
  public TopSnackbar setText(@NonNull CharSequence message) {
    final SnackbarContentLayout contentLayout = (SnackbarContentLayout) view.getChildAt(0);
    final TextView tv = contentLayout.getMessageView();
    tv.setText(message);
    return this;
  }

  /**
   * Update the text in this {@link TopSnackbar}.
   *
   * @param resId The new text for this {@link BaseTransientBottomBar}.
   */
  @NonNull
  public TopSnackbar setText(@StringRes int resId) {
    return setText(getContext().getText(resId));
  }

  /**
   * Set the action to be displayed in this {@link BaseTransientBottomBar}.
   *
   * @param resId String resource to display for the action
   * @param listener callback to be invoked when the action is clicked
   */
  @NonNull
  public TopSnackbar setAction(@StringRes int resId, View.OnClickListener listener) {
    return setAction(getContext().getText(resId), listener);
  }

  /**
   * Set the action to be displayed in this {@link BaseTransientBottomBar}.
   *
   * @param text Text to display for the action
   * @param listener callback to be invoked when the action is clicked
   */
  @NonNull
  public TopSnackbar setAction(
      @Nullable CharSequence text, @Nullable final View.OnClickListener listener) {
    final SnackbarContentLayout contentLayout = (SnackbarContentLayout) this.view.getChildAt(0);
    final TextView tv = contentLayout.getActionView();
    if (TextUtils.isEmpty(text) || listener == null) {
      tv.setVisibility(View.GONE);
      tv.setOnClickListener(null);
      hasAction = false;
    } else {
      hasAction = true;
      tv.setVisibility(View.VISIBLE);
      tv.setText(text);
      tv.setOnClickListener(
          new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              listener.onClick(view);
              // Now dismiss the Snackbar
              dispatchDismiss(BaseCallback.DISMISS_EVENT_ACTION);
            }
          });
    }
    return this;
  }

  @Override
  @Duration
  public int getDuration() {
    int userSetDuration = super.getDuration();
    if (userSetDuration == LENGTH_INDEFINITE) {
      return LENGTH_INDEFINITE;
    }

    if (VERSION.SDK_INT >= VERSION_CODES.Q) {
      int controlsFlag = hasAction ? FLAG_CONTENT_CONTROLS : 0;
      return accessibilityManager.getRecommendedTimeoutMillis(
          userSetDuration, controlsFlag | FLAG_CONTENT_ICONS | FLAG_CONTENT_TEXT);
    }

    // If touch exploration is enabled override duration to give people chance to interact.
    return hasAction && accessibilityManager.isTouchExplorationEnabled()
        ? LENGTH_INDEFINITE
        : userSetDuration;
  }

  /**
   * Sets the text color of the message specified in {@link #setText(CharSequence)} and {@link
   * #setText(int)}.
   */
  @NonNull
  public TopSnackbar setTextColor(ColorStateList colors) {
    final SnackbarContentLayout contentLayout = (SnackbarContentLayout) view.getChildAt(0);
    final TextView tv = contentLayout.getMessageView();
    tv.setTextColor(colors);
    return this;
  }

  /**
   * Sets the text color of the message specified in {@link #setText(CharSequence)} and {@link
   * #setText(int)}.
   */
  @NonNull
  public TopSnackbar setTextColor(@ColorInt int color) {
    final SnackbarContentLayout contentLayout = (SnackbarContentLayout) view.getChildAt(0);
    final TextView tv = contentLayout.getMessageView();
    tv.setTextColor(color);
    return this;
  }

  /**
   * Sets the text color of the action specified in {@link #setAction(CharSequence,
   * View.OnClickListener)}.
   */
  @NonNull
  public TopSnackbar setActionTextColor(ColorStateList colors) {
    final SnackbarContentLayout contentLayout = (SnackbarContentLayout) view.getChildAt(0);
    final TextView tv = contentLayout.getActionView();
    tv.setTextColor(colors);
    return this;
  }

  /**
   * Sets the text color of the action specified in {@link #setAction(CharSequence,
   * View.OnClickListener)}.
   */
  @NonNull
  public TopSnackbar setActionTextColor(@ColorInt int color) {
    final SnackbarContentLayout contentLayout = (SnackbarContentLayout) view.getChildAt(0);
    final TextView tv = contentLayout.getActionView();
    tv.setTextColor(color);
    return this;
  }

  public TopSnackbar fitSystemWindows(){
    int height = 0;
    Resources resources = view.getResources();
    if (Build.MANUFACTURER.toLowerCase().equals("xiaomi")){
      int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
      if (resourceId > 0) {
        height =  resources.getDimensionPixelSize(resourceId);
      }
    }else {
      try {
        Class<?> c = Class.forName("com.android.internal.R$dimen");
        Object obj = c.newInstance();
        Field field = c.getField("status_bar_height");
        int x = Integer.parseInt(field.get(obj).toString());
        if(x > 0){
          height =  resources.getDimensionPixelSize(x);
        }
      } catch (Exception e) {
        e.printStackTrace();
        return this;
      }
    }
    view.setPadding(view.getPaddingLeft(),view.getPaddingTop()+height,view.getPaddingRight(),view.getPaddingBottom());
    return this;
  }

  /** Sets the tint color of the background Drawable. */
  @NonNull
  public TopSnackbar setBackgroundTint(@ColorInt int color) {
    Drawable background = view.getBackground();
    if (background != null) {
      background = background.mutate();
      // Drawable doesn't implement setTint in API 21 and Snackbar does not yet use
      // MaterialShapeDrawable as its background (i.e. TintAwareDrawable)
      if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP_MR1) {
        DrawableCompat.setTint(background, color);
      } else {
        background.setColorFilter(color, PorterDuff.Mode.SRC_IN);
      }
    }
    return this;
  }

  /** Sets the tint color state list of the background Drawable. */
  @NonNull
  public TopSnackbar setBackgroundTintList(ColorStateList colorStateList) {
    DrawableCompat.setTintList(view.getBackground().mutate(), colorStateList);
    return this;
  }

  /**
   * Set a callback to be called when this the visibility of this {@link TopSnackbar} changes. Note
   * that this method is deprecated and you should use {@link #addCallback(BaseCallback)} to add a
   * callback and {@link #removeCallback(BaseCallback)} to remove a registered callback.
   *
   * @param callback Callback to notify when transient bottom bar events occur.
   * @deprecated Use {@link #addCallback(BaseCallback)}
   * @see Callback
   * @see #addCallback(BaseCallback)
   * @see #removeCallback(BaseCallback)
   */
  @Deprecated
  @NonNull
  public TopSnackbar setCallback(@Nullable Callback callback) {
    // The logic in this method emulates what we had before support for multiple
    // registered callbacks.
    if (this.callback != null) {
      removeCallback(this.callback);
    }
    if (callback != null) {
      addCallback(callback);
    }
    // Update the deprecated field so that we can remove the passed callback the next
    // time we're called
    this.callback = callback;
    return this;
  }

  /**
   * @hide Note: this class is here to provide backwards-compatible way for apps written before the
   *     existence of the base {@link BaseTransientBottomBar} class.
   */
  @RestrictTo(LIBRARY_GROUP)
  public static final class SnackbarLayout extends SnackbarBaseLayout {
    public SnackbarLayout(Context context) {
      super(context);
    }

    public SnackbarLayout(Context context, AttributeSet attrs) {
      super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
      // Work around our backwards-compatible refactoring of Snackbar and inner content
      // being inflated against snackbar's parent (instead of against the snackbar itself).
      // Every child that is width=MATCH_PARENT is remeasured again and given the full width
      // minus the paddings.
      int childCount = getChildCount();
      int availableWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
      for (int i = 0; i < childCount; i++) {
        View child = getChildAt(i);
        if (child.getLayoutParams().width == ViewGroup.LayoutParams.MATCH_PARENT) {
          child.measure(
              MeasureSpec.makeMeasureSpec(availableWidth, MeasureSpec.EXACTLY),
              MeasureSpec.makeMeasureSpec(child.getMeasuredHeight(), MeasureSpec.EXACTLY));
        }
      }
    }
  }
}

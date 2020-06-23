# TopSnackBar

##Support the top pop-up snapbar, based on Android x, support the current highest version, and adapt to the transparent status bar

Some different top pop-up snapbar are tried, but there is no animation effect in the higher version, so I copied it from the source code and modified a snapbar that supports the top pop-up of the higher version of animation. The source code comes from the snapbar. Based on Android x, it supports the same functions as the current highest version of snapbar.
you can ues like this :
Topsnackbar.make (view,"string",duration).show(); //Pop up under statusbar 
Topsnackbar.make ( view.getRootView (), "string", duration. Show(); // pop up from the statusbar, that is, overwrite the status bar (fitwindows has been processed in the layout)

If there is a problem with the display, add fitsystemwindows()

for example

Topsnackbar.make ( view.getRootView (),"string",duration). fitSystemWindows.show ();


# 中文
# TopSnackBar
## 支持顶部弹出的snackbar，基于android x，支持目前最高版本,适配透明状态栏

试用过一些其他的顶部弹出snackbar,但是在高版本没有动画效果，所以就自己从源码复制然后修改了一份支持高版本动画的 支持顶部弹出的snackbar，源码来自snackbar，基于android x，支持目前最高版本 功能与snackbar一样 Topsnackbar.make(view,"string",duration).show(); //在statusbar下方弹出 Topsnackbar.make(view.getRootView(),"string",duration).show(); //从statusbar弹出，即覆盖通知栏(layout里已经做了fitwindows处理)
如果显示有问题，可以再加上fitSystemWindows()
例如
Topsnackbar.make(view.getRootView(),"string",duration).fitSystemWindows.show();

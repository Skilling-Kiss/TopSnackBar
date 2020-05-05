# TopSnackBar
支持顶部弹出的snackbar，基于android x，支持目前最高版本

因为用了一个其他的顶部弹出snackbar,但是在高版本没有动画效果，所以就自己从源码复制然后修改了一份支持高版本动画的 支持顶部弹出的snackbar，源码来自snackbar，基于android x，支持目前最高版本 功能与snackbar一样 Topsnackbar.make(view,"string",duration).show(); //在statusbar下方弹出 Topsnackbar.make(view.getRootView(),"string",duration).show(); //从statusbar弹出，即覆盖通知栏(layout里已经做了fitwindows处理)

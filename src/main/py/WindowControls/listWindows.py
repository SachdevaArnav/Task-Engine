import psutil,win32gui
from pywinauto import Desktop, win32functions
MIN_WIDTH  = 50
MIN_HEIGHT = 50
def check_rect(hwnd):
    try:
        left, top, right, bottom = win32gui.GetWindowRect(hwnd)
        return  right - left>=MIN_WIDTH and  bottom - top >=MIN_HEIGHT
    except:
        return False

def is_cloaked(hwnd):
    try:
        DWMWA_CLOAKED = 14
        cloaked = win32gui.DwmGetWindowAttribute(hwnd, DWMWA_CLOAKED)
        return cloaked != 0
    except:
        return False
def run():
    #We are using win32 backend only because
    #If a desktop app can be minimized and restored, it has an HWND.
    #If it has an HWND, Win32 can enumerate it.
    #That's why all apps irrespective UIA or Win32 already part of this list
    wins=Desktop(backend="win32").windows(visible_only=True)
    #First we need to find the hwnd of the process we are trying to bring forward
    #But all decisions about choices and selections are made by Engine only
    #hence this part will only send the informations needed for deicision making
    list=[]
    for win in wins:
        # These 3 acts as filter to remove fantom windows,
        # windows not in workable mode for now or which got destroyed after wins creation
        if not win32functions.IsWindow(win.handle):
            continue
        if win32functions.GetParent(win.handle)==0:
            continue
        if not check_rect(win.handle):
            continue
        if is_cloaked(win.handle):
            continue
        pid=win.element_info.process_id
        process= psutil.Process(pid)
        if not process.is_running():
            continue
        list.append({"hwnd":win.handle,
                  "pid":pid,
                  "WindowTitle":win.window_text().lower(),
                  "process":process.name().lower()
                  })
    return {
        "status":"OK",
        "windows":list
    }
# IMPORTANT FOR THE ENGINE many process have multiple hwnd even after we select visible_only=True
# in these always consider those processes first which have Window Title
# We dont filter them out just give a lower score

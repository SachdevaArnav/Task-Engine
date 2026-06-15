from MouseControls.buttonUp import INPUT, MOUSEINPUT
import ctypes
MOUSEEVENTF_WHEEL = 0x0800
SendInput = ctypes.windll.user32.SendInput
def run(delta):
    for _ in range(2):
        inp=INPUT(0,MOUSEINPUT(0,0,delta,MOUSEEVENTF_WHEEL,0,None))
        result=SendInput(1,ctypes.byref(inp),ctypes.sizeof(inp))
        if result==1:
            return {
                "status":"OK"
            }
    return{
        "status":"error",
        "error":"Scrolling failed"
    }

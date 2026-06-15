import ctypes
from MouseControls import buttonDown,buttonUp
from MouseControls.buttonUp import MOUSEINPUT, INPUT
def run(button="left"):
    # time.sleep(10)
    try:
        # tryDown=buttonDown.run(button)
        # if tryDown.get("status")=="error":
        #     raise Exception(tryDown.get("error"))
        # time.sleep(0.01) #10ms delay to prevent false failures
        # tryUp=buttonUp.run(button)
        # if tryUp.get("status")=="error":
        #     raise Exception(tryUp.get("error"))
        buttonUpCode=0x0004 #Left-up
        buttonDownCode=0x0002 #Left-down
        if(button=="right"):
            buttonUpCode=0x0010 #Right-up
            buttonDownCode=0x0008 #Right-down
        for _ in range(2):
            inputs=(INPUT*2)()
            inputs[0] = INPUT(0, MOUSEINPUT(0, 0, 0, buttonDownCode, 0, None))  # DOWN
            inputs[1] = INPUT(0, MOUSEINPUT(0, 0, 0, buttonUpCode, 0, None))  # UP
            SendInput=ctypes.windll.user32.SendInput
            if SendInput(2,inputs,ctypes.sizeof(INPUT))==2:
                return {
                "status":"OK"
                }
            buttonUp.run(button)
        raise Exception("Click attempt failed")
    except Exception as e:
        buttonUp.run(button)
        return {
            "status":"error",
            "error":"Click attempt failed; state UNKNOWN (could be changed or not). future Planner level escalation might be required; check environmental state"
        }

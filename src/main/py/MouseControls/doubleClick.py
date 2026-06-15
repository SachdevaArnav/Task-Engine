import ctypes
from MouseControls.buttonUp import MOUSEINPUT, INPUT
from MouseControls import buttonUp
def run(button="left"):
    try:
        buttonUpCode=0x0004 #Left-up
        buttonDownCode=0x0002 #Left-down
        if(button=="right"):
            buttonUpCode=0x0010 #Right-up
            buttonDownCode=0x0008 #Right-down
        #we are passing all the 4 inputs in one SendInput Query
        #instead of composition buttonUp and ButtonDown atoms
        #because Execution safety prefers batching over composition
        #because composition can break higher-level gestures
        for _ in range(2):
            inputs=(INPUT*4)()
            inputs[0] = INPUT(0, MOUSEINPUT(0, 0, 0, buttonDownCode, 0, None))  # DOWN
            inputs[1] = INPUT(0, MOUSEINPUT(0, 0, 0, buttonUpCode, 0, None))  # UP
            inputs[2] = INPUT(0, MOUSEINPUT(0, 0, 0, buttonDownCode, 0, None))  # DOWN
            inputs[3] = INPUT(0, MOUSEINPUT(0, 0, 0, buttonUpCode, 0, None))  # UP
            SendInput=ctypes.windll.user32.SendInput
            if SendInput(4,inputs,ctypes.sizeof(INPUT))==4:
                return {
                "status":"OK"
                }
            buttonUp.run(button)
        raise Exception("Double click attempt failed")
    except Exception as e:
        return {
            "status":"error",
            "error":"Double Click attempt failed; state UNKNOWN (could be changed or not). future Planner level escalation might be required; check environmental state"
        }


from UiTreeControls import inspectWindow
from pywinauto.controls.uiawrapper import UIAWrapper

def getTargetObject(handle,target):
    list=inspectWindow.run(handle,sendRealObjects=True)["list"]
    for node in list:
        if node["automation_id"]==target["automation_id"] and node["name"]==target["name"] and node["control_type"]==target["control_type"] and node["class_name"]==target["class_name"]:
            return node["obj"]

def run(handle,target):
    try:
        targetObj=getTargetObject(handle,target)
        control_type=(target["control_type"]).lower().strip()
        wrapper=UIAWrapper(targetObj)
        try:
            wrapper.invoke()
            return {
            "status":"OK"
            }
        except:
            pass

        try:
            wrapper.select()
            return {
            "status":"OK"
            }
        except:
            pass

        try:
            wrapper.toggle()
            return {
            "status":"OK"
            }
        except:
            pass

        try:
            wrapper.click_input()
            return {
            "status":"OK"
            }
        except:
            pass
        # try:
        # Another final fallback method could include using the coords we get during inspection by
        # first bringing the desired window to front
        # To do that first fix drift in value of coords we r getting wrt physical pixels
    except Exception as e:
        return{
            "status":"error",
            "error":str(e),
            "type":type(e)
        }
if __name__=="__main__":
    print("hello")
    dict={"name":"Bold (Ctrl+B)","control_type":"Button","automation_id":"","class_name":"ToggleButton","enabled":True,"visible":True,"centerX":801,"centerY":483,"height":40,"width":40,"parentName":""}
    print(run(1182166,dict))
    print("bye")


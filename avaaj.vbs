Dim message, sapi
  message = "Workflow completed successfully"
 Set sapi = CreateObject("sapi.spvoice")
sapi.Speak message
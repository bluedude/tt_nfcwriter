Type=Activity
Version=3.8
B4A=true
@EndOfDesignText@
#Region  Activity Attributes 
	#FullScreen: True
	#IncludeTitle: True
#End Region

Sub Process_Globals
	'These global variables will be declared once when the application starts.
	'These variables can be accessed from all modules.
	#Region Variable declarations
		Dim nfcType As String
	#End Region 
End Sub
Sub Globals
	'These global variables will be redeclared each time the activity is created.
	'These variables can only be accessed from this module.
	#Region Library declarations
		Dim NFCTagWriter As RSNFCTagWriter
	#End Region 
	#Region Views declarations
		'Declaration of views
		'frmMain
		Dim txtTag As EditText 
	#End Region 
End Sub
Sub Activity_Create(FirstTime As Boolean)
	
	'Do not forget to load the layout file created with the visual designer. For example:
	Activity.LoadLayout ("frmMain")
	NFCTagWriter.Initialize("NFCTagWriter")
	
End Sub
Sub Activity_Resume
	
	'you need to catch the NFC stuff in Activity Resume because NFC events by the device are external activities
	NFCTagWriter.EnableForegroundDispatch
	Dim nfcIntent As Intent
 	nfcIntent = Activity.GetStartingIntent
	If nfcIntent <> Null Then
		Dim Tag As RSNFCTag
    	Tag = NFCTagWriter.getTag(nfcIntent)
		If Tag.IsInitialized = True AND Tag <> Null Then
			ProgressDialogHide
			If Write(Tag, txtTag.Text) = True Then
				NFCTagWriter_Success
			Else
				NFCTagWriter_Error(LastException.Message)
			End If
	 	End If
	End If

End Sub
Sub Activity_Pause (UserClosed As Boolean)
	NFCTagWriter.DisableForegroundDispatch
End Sub
'button that starts the writing procedure
Sub btnWrite_Click
	'set NDEF type to URI or TEXT
	nfcType = "txt"
	ProgressDialogShow("Hold tag against device.")
End Sub
Sub createTextNDEF(Text As String) As RSNFCRecord

	Dim lang As String = "en"
	Dim textBytes() As Byte = Text.GetBytes("UTF8")
	Dim langBytes() As Byte = lang.GetBytes("US-ASCII")
	Dim langLength As Int = langBytes.Length
	Dim textLength As Int = textBytes.Length
	
	Dim payload(1 + langLength + textLength) As Byte
	payload(0) = langLength
	
	NFCTagWriter.ArrayCopy(langBytes, 0, payload, 1, langLength)
	NFCTagWriter.ArrayCopy(textBytes, 0, payload, 1 + langLength, textLength)

	Dim recordNFC As RSNFCRecord
	Dim b(0) As Byte
	recordNFC.Initialize(recordNFC.TNF_WELL_KNOWN, recordNFC.RTD_TEXT, b, payload)
	Return recordNFC

End Sub
Sub createURINDEF (Text As String) As RSNFCRecord

	Dim textBytes() As Byte = Text.GetBytes("UTF8")
	Dim textLength As Int = textBytes.Length
	
	Dim payload(1 + textLength) As Byte
	'sample payloads
	'0x1	=	http://www.
	'0x2	=	https://www.
	'0x3	=	http://
	
	'set http payload
	payload (0) = 0x3	
	NFCTagWriter.ArrayCopy(textBytes, 0, payload, 1, textLength)

	'create record
	Dim recordNFC As RSNFCRecord 
	Dim b(0) As Byte
	recordNFC.Initialize(recordNFC.TNF_WELL_KNOWN, recordNFC.RTD_URI, b, payload)
	Return recordNFC

End Sub
Sub Write (Tag As RSNFCTag, Text As String) As Boolean
	
	Dim Message As RSNFCMessage
	Dim Ndef As RSNFCNdef
	
	Select Case nfcType
	Case "txt"
		'txt NDEF
		Message.Initialize(createTextNDEF(Text))
	Case "uri"
		'URI NDEF
		Message.Initialize(createURINDEF(Text))
	Case Else
	End Select
	
	Ndef.Initialize(Tag)
	ProgressDialogHide
	If Ndef.IsInitialized = True Then
		Try
			Ndef.Connect
			Ndef.WriteNdefMessage(Message)
			Ndef.Close
			Return True
		Catch
			Log(LastException.Message)
			Return False
		End Try
	Else
		Dim NdefFormatable As RSNFCNdefFormatable
		NdefFormatable.Initialize(Tag)
		
		If NdefFormatable.IsInitialized = True Then
			Try
				NdefFormatable.Connect
				NdefFormatable.Format(Message)
				NdefFormatable.Close
				Return True
			Catch
				Log(LastException.Message)
				Return False
			End Try
			
		End If
	End If

		
	Return False
	
End Sub
#Region NFC events
	'Succesfully wrote to the NFC tag.
	Sub NFCTagWriter_Success
		ToastMessageShow("Tag has been written!", False)
	End Sub

	'An error happened when writing to the NFC tag.
	Sub NFCTagWriter_Error (Message As String)
		ToastMessageShow("Error writing tag: " & Message, False)
	End Sub
#End Region 

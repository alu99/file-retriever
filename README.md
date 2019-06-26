# file-retriever
Javax mail project that fetches a file from your computer and emails it back as an attachment

How it works: 
1. Email FileRetriever with the absolute filepath of the file you want to retrieve.
2. FileRetriever will search its host computer's directory for that file
3. If file is found, emails sender with the file attatched

FileRetriever's email account must be a Gmail. Change the email type to plain text when emailing the filepath to FileRetriever. 

In order to let FileRetriever interact with the sender's Gmail account, you must allow less secure apps to access that account. The recipient's email can just be your everyday email account.

How to allow access to less secure apps: https://support.google.com/accounts/answer/6010255?hl=en

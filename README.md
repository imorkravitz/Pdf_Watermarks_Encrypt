# Pdf_Watermarks_Encrypt
Encrypt Pdf files from destination source with spire library which has limit of 10 pages and add watermarks. split by the files name 


EncryptWatermark_pdf This project allow get a list of pdf files from a folder and secure all and put watermarks by the name of each file name all pages each file has a number of pages - this project make all pages watermarks with the full name of a Client (pdf name until reach to "_") and encrypted the file with password that make the file be closed for editing and copying only allows to print

alias nomore='find ./ -iname .DS_Store -delete' *
in terminal will prevent crea of DS_Store invisible file which cause problem with the iteration

-
using spire library
-

in terminal: command >> nana orchestrate_pdf_processing.sh

#!/bin/bash

LOG_FILE="/Users/orkravitz/dev/orchestrate_pdf_processing.log"

{
    echo "$(date) - Starting word files to PDF script"
    /Library/Frameworks/Python.framework/Versions/3.10/bin/python3 /Users/orkravitz/dev/wordFilesToPdf/wordFilesToPdfFromDrive.py
    echo "$(date) - Completed word files to PDF script"

    echo "$(date) - Starting split PDF script"
    /Library/Frameworks/Python.framework/Versions/3.10/bin/python3 /Users/orkravitz/dev/Pdf_Split_Merge_Encryp/PDF_Split_and_Merge/SplitPdfFiles.py
    echo "$(date) - Completed split PDF script"

    echo "$(date) - Starting encrypt PDF script"
    /usr/bin/java -cp /Users/orkravitz/dev/Pdf_Encrypt_Watermarks/PDF_Watermarks/lib/Spire.Pdf.jar /Users/orkravitz/dev/Pdf_Encrypt_Watermarks/PDF_Watermarks/src/EncryptPDF.java
    echo "$(date) - Completed encrypt PDF script"

    echo "$(date) - Starting merge PDF script"
    /Library/Frameworks/Python.framework/Versions/3.10/bin/python3 /Users/orkravitz/dev/Pdf_Split_Merge_Encryp/PDF_Split_and_Merge/MergePdfFiles_Encrypt.py
    echo "$(date) - Completed merge PDF script"


    echo "$(date) - Completed all tasks"
} >> "$LOG_FILE" 2>&1

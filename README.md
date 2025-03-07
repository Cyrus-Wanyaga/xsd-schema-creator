# XSD Schema Creator

## What is it?

This is a small Java application/script that reads .docx solution documents for SOA services. It checks for sample output for the services and uses this information to create an XSD schema which is then used in the integration tool. 

## Why/Motivations

The output schema is defined in these documents in the form of 
<b>Field1.Field1a.Field1ai</b>

These fields can be in the hundreds. Manual conversion was bound to take up too much time, prompting the need for this tool to be developed. 


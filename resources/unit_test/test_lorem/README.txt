REM NOTE: should have removed this with the remove-all MySQL series of statements, prior to attempting this test.
REM  LLF, 7/12/2011

REM to test initial project creation.
..\..\load_single.bat Project.tsv
..\..\load_single.bat ProjectMetaAttributes.tsv
..\..\load_single.bat ProjectRegistration_LookupValues.tsv
..\..\load_single.bat ProjectRegistration_EventAttributes.tsv
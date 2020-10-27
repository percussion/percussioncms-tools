@echo off
REM *
REM * $Id: TestBitmapManager.bat 1.1 1999/02/02 22:39:47Z PaulHoward Release $
REM *
REM * Version Labels  : $Name: Pre_CEEditorUI RX_40_REL 20010618_3_5 20001027_3_0 20000724_2_0 20000522_1_1 20000501_1_1 20000327_1_1 20000111_1_0 991227_1_0 991214_1_0 991213_1_0 991202_1_0 Release_1_0_Intl Release_10_1 Release_10 $
REM *
REM * Locked By       : $Locker: $
REM *
REM * Revision History:
REM *
REM *                   $Log: TestBitmapManager.bat $
REM *                   Revision 1.1  1999/02/02 22:39:47Z  PaulHoward
REM *                   Initial revision
REM *

REM This batch file sets up the image files necessary to unit test BitmapManager
REM class. Extract test.gif from the archives, then run this batch file.

copy test.gif test2.gif
copy test.gif test3.gif
copy test.gif test4.gif
copy test.gif test5.gif
copy test.gif test6.gif
IF NOT EXIST images THEN md images
copy test.gif images
copy images\test.gif images\test7.gif
copy test.gif c:\test8.gif


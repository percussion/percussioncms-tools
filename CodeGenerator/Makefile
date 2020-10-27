#
# Makefile for the server component of E2. It specifies all the packages.
#
# $Id: Makefile 1.1 1999/09/22 03:02:26Z markdandrea Release $
#
# Version Label   : $Name: PreDbPubBEA Pre_CEEditorUI RX_40_REL 20010618_3_5 20001027_3_0 20000724_2_0 20000522_1_1 20000501_1_1 20000327_1_1 20000111_1_0 991227_1_0 991214_1_0 991213_1_0 991202_1_0 Release_1_0_Intl Release_10_1 Release_10 $
#
# Locked By       : $Locker: $
#
# Revision History:
#
#                   $Log: Makefile $
#                   Revision 1.1  1999/09/22 03:02:26Z  markdandrea
#                   Initial revision
#                   Revision 1.1  1999/08/20 23:41:11Z  markdandrea
#                   Initial revision
#                   Revision 1.2  1999/08/10 22:40:50  markdandrea
#                   Revision 1.1  1999/08/10 14:56:59  markdandrea
#                   Initial revision
#                   Revision 1.2  1999/07/20 14:55:03  markdandrea
#                   Fixed how it looked for source.
#                   
#                   Revision 1.1  1999/07/19 23:13:20  markdandrea
#                   Initial revision
#                   
#
#

JARFILENAME=codegenerator.jar


SOURCE_INSTALLER             = com$(DIRSEP)percussion$(DIRSEP)CodeGenerator

PACKAGEDIRS = $(SOURCE_INSTALLER)

PACKAGES = $(PACKAGEDIRS:s~$(DIRSEP)~.~)

PACKAGEFILES    = $(SOURCE_INSTALLER:+"$(DIRSEP)*.java")

PROPERTY_FILES = com$(DIRSEP)percussion$(DIRSEP)CodeGenerator$(DIRSEP)*.properties

.INCLUDE : ../../builtins.mak

# Overrides of builtins.mak settings
DOCTITLE  = "E2 1.0 Internal API"


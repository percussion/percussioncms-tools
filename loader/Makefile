#
# Makefile for the designer component of E2. It specifies all the packages.
#

JARFILENAME = rxloader.jar
VERSIONFILENAME = com$(DIRSEP)percussion$(DIRSEP)loader$(DIRSEP)Version.properties

PROPERTY_FILES	=  \
   com$(DIRSEP)percussion$(DIRSEP)loader$(DIRSEP)*.properties \
   com$(DIRSEP)percussion$(DIRSEP)loader$(DIRSEP)ui$(DIRSEP)*.properties

IMAGE_FILES = \
   com$(DIRSEP)percussion$(DIRSEP)loader$(DIRSEP)ui$(DIRSEP)images$(DIRSEP)*.gif

SOURCE_LOADER     = com$(DIRSEP)percussion$(DIRSEP)loader
SOURCE_UI         = com$(DIRSEP)percussion$(DIRSEP)loader$(DIRSEP)ui
SOURCE_SELECTOR   = com$(DIRSEP)percussion$(DIRSEP)loader$(DIRSEP)selector
SOURCE_OBJECT     = com$(DIRSEP)percussion$(DIRSEP)loader$(DIRSEP)objectstore
SOURCE_EXTRACTOR  = com$(DIRSEP)percussion$(DIRSEP)loader$(DIRSEP)extractor
SOURCE_UTIL  = com$(DIRSEP)percussion$(DIRSEP)loader$(DIRSEP)util

PACKAGEDIRS = \
   $(SOURCE_LOADER) \
   $(SOURCE_UI) \
   $(SOURCE_SELECTOR) \
   $(SOURCE_OBJECT) \
   $(SOURCE_EXTRACTOR) \
   $(SOURCE_UTIL)
   
IMAGEDIR = $(SOURCE_UI)$(DIRSEP)images

PACKAGES = $(PACKAGEDIRS:s~$(DIRSEP)~.~)

IMAGES = $(IMAGEDIR:s~$(DIRSEP)~.~)

PACKAGEFILES = \
   $(SOURCE_LOADER:+"$(DIRSEP)*.java") \
   $(SOURCE_UI:+"$(DIRSEP)*.java") \
   $(SOURCE_SELECTOR:+"$(DIRSEP)*.java") \
   $(SOURCE_OBJECT:+"$(DIRSEP)*.java") \
   $(SOURCE_EXTRACTOR:+"$(DIRSEP)*.java") \
   $(SOURCE_UTIL:+"$(DIRSEP)*.java") 

   
.IF $(OS) == unix
   DIRSEP= /
.ELSE
   DIRSEP= \ 
.END

.INCLUDE : ..$(DIRSEP)builtins.mak

# Overrides of builtins.mak settings
DOCTITLE  = "Rhythmyx Content Loader 1.0 Internal API"

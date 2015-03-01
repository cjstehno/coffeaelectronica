title=Export Eclipse Working Sets
date=2007-12-07
type=post
tags=blog,java
status=published
~~~~~~
I came across a question related to this on [EclipseZone](http://eclipsezone.com/) where I posted a
[response](http://eclipsezone.com/eclipse/forums/t104065.html) (and a follow-up). It seemed like
something that I should blog about for future reference.

## Export Working Set Definition

If you just want to share your working set definition with other members on your team, it's very easy.

1. Via the menu bar: File &gt; "Export..."
1. Select "General" / "Working Sets"
1. Click "Next &gt;"
1. Select the working set(s) you want to export and where you want to export the file.
1. Click "Finish" to perform the export.

You will have a file containing the project-based definition of your working set. This can be imported by your team so
that they can have the same working set, as long as they have the files that it represents with the same paths.
Generally, this is probably a safe assumption for people on the same team using the same IDE.

## Export Working Set Files

If you want to export the actual files contained in your working set, the steps are a little different, but they make
sense once you think about it.

> _Note:_ I generally have the "Top Level Emements" of my views set to "Working Sets", so this is based on that
assumption. It is easy to toggle (View arrow menu).

1. Right-click on the working set you want to export
1. Select "Export..."
1. Select "File System" (jar and archive will probably work with this too)
1. You will then have the File system export dialog with your working set pre-populated.
1. Select the directory you want to export to
1. You can either export only the directories explicitly defined in your working set using "Create only selected directories" or you can create any missing parent directories using "Create directory structure for files".
1. Click Finish and you have your exported files.

This method does not export the working set definition, just the files themselves. Using both methods you could export
the working set and the files so that your team can have everything they need. These techniques are useful when sharing
files or when you want to extract a component and create a new project from it.

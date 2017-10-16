# Dynamic Mount

PeripheralsPlusOne includes a ComputerCraft package manager for Lua scripts called "Dyn". Its functionality is inspired
 by Debian's "Advanced Package Tool" (APT) and thus, shares similar commands.

# Dyn(amic) Package Manager

The **Dyn**(amic) package manager makes managing and distributing ComputerCraft scripts easier. Programs installed via
 Dyn are automatically mounted when a peripheral is attached and the program supports the peripheral type. The mount
 path is `/rom/programs/` so the programs will be executable from any directory. Dyn also manages additional files
 and help text. Dyn will not override default programs.
 
## Repos

Dyn knows about available packages by reading index files from repositories. For more information about the structure
 of a repository view the [default Dyn repo].
 
Like APT repos, Dyn allows for multiple sources to be added to its sources list. The URL should be the root path that
 the index file resides.
 
## Commands

### install <program name\>

Attempts to download the specified program

### remove <program name\>

Attempts to remove the specified program

### update

Updates the local program indices from their repos

This command will fail if a repo contains an invalid index file or there is an issue downloading the file.

Programs will not be updated by this. This only updates the list of available programs

### upgrade

Upgrades all installed programs to the latest version

### list

List all installed programs

### search

Search for a program by name or description

The search looks in all repos defined in the sources file

### repo <argument\>

Sub-command for working with repos

#### add <repo url\> \[friendly name\]

Adds a repo to be used for program resolving

#### remove <repo url/friendly name\>

Removes a repo from the list of available sources

#### edit

Opens the sources file for editing



[default Dyn repo]: https://github.com/rolandoislas/dyn
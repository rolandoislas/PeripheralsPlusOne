# RFID Reader/Writer

The RFID Reader/Writer allows for writing to and reading from [RFID Chips].

## Data Format

The data format complies to the format of a [MIFARE Classic] contactless smart card, however, value blocks and their
 functions (increment, decrement, restore, transfer) are not implemented.
 
### Quick Overview

#### Data Blocks

Each card has 16 sectors, each with 4 blocks of 16 bytes. The first block is a manufacture block, is read-only 
 (regardless of access permissions), and contains the UUID of the chip in its first 7 bytes. The last block in each
 sector is named the sector trailer block and contains the two keys for the sector and the bits that determine access
 permissions.

##### UUID
 
Each ID is guaranteed to be unique at the time of crafting. Duplicating the item (e.g. creative mode) can create
 a situation where the ID is no longer unique to one chip. The range of IDs is 0x00-0xffffffffffffff. IDs will loop back
 to zero after the max has been reached, causing new chips to no longer be created with unique IDs.
 
#### Access Permissions/Keys \[Sector Trailer\]

The first 6 bytes of a sector trailer block make up key A. The last 6 bytes make up key B. The 3 bytes after key A
 are used to determine the access bits. One byte after the access bytes can be used as data storage. By default key A
 has permissions to read and write to all blocks in the sector, but it can never read itself. Key B will not be able to
 be used for authentication since it can be read by key A.
 
View the [MIFARE Classic] documentation section 8.7.1 for detailed information about the access bits.
 
**Note: Care should be taken when writing to a sector trailer as setting incorrect access bits will lock the sector
 permanently.**
 
**Note: Changing a key will take effect immediately.**

## Functions

<table>
  <tr>
    <th>Function</th>
    <th>Returns</th>
    <th>Description</th>
  </tr>
  <tr>
    <td>search()</td>
    <td><i>table</i> uid</td>
    <td>Searches for any RFID chip (or chipped item) within a radius of 3 blocks and returns its ID. First, entities' 
     inventories will be searched. Then, tile entities will be searched. If a valid item is in the reader/writer slot,
     it will take priority. Returns nil if no item was found. The id will be a table of bytes[1].</td>
  </tr>
  <tr>
    <td>select(<i>table</i> uid)</td>
    <td>nil</td>
    <td>Specifies an RFID chip that will be used by other commands. The uid should be a table of seven 
     bytes[1]</td>
  </tr>
  <tr>
    <td>auth(<i>number</i> key_type, <i>number</i> block, <i>table</i> key)</td>
    <td>nil</td>
    <td>Specifies a key to use for authentication. Valid key types are 0 and 1 for keys A or B. The specified block
     is used to determine which sector this key can be used to authenticate against. The key should be a table of
     six bytes[1].</td>
  </tr>
  <tr>
  <tr>
    <td>deauth()</td>
    <td>nil</td>
    <td>Clears the stored authentication key</td>
  </tr>
  <tr>
    <td>read(<i>number</i> block)</td>
    <td><i>table</i> block_data</td>
    <td>Attempts to read the specified block. If there is no auth key, no chip has been selected, or the selected chip
     cannot be found, nil will be returned. Otherwise, a table of 16 bytes[1] will always be returned. The bytes 
     returned can be zeros depending on the access permissions for the block.</td>
  </tr>
  <tr>
    <td>write(<i>number</i> block, <i>table</i> data)</td>
    <td><i>boolean</i> success</td>
    <td>Attempts to write data to a block. If there is no auth key, no chip has been selected, or the chip cannot be
     found, false will be returned. The write operation will fail silently if authentication fails. Bytes are only 
     written if the access permissions for the block allow it.</td>
  </tr>
</table>

1. unsigned integer with the range 0-255

# Search Pattern

The RFID reader/writer begins its search at a one block radius and continues to expand until either an RFID item is
 found or the max search radius has been reached.

The following list shows the order that the RFID reader/writer will take when searching for RFID items per block.

1. Entities
    1. Player Inventories
    2. Items In-World
    3. Items Held by Entities
    4. Embedded Tags. See [RFID Chip Prod]
2. Tile Entity Inventories



[RFID Chips]: /miscellaneous_additions/rfid_chip.md
[MIFARE Classic]: https://www.nxp.com/docs/en/data-sheet/MF1S50YYX_V1.pdf
[RFID Chip Prod]: /miscellaneous_additions/rfid_chip_prod.md
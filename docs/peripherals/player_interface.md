# Player Interface

The player interface allows a player's inventory to be manipulated.

It requires a [Permissions Card](/miscellaneous_additions/permissions_card) to take actions on an inventory. The card
 fits into the internal inventory.


# Functions

|Function|Returns|Description|
|-|-|-|
|getPlayerInv(_string_ player_name)|_table_ player_inventory_object|Returns an object representing a players inventory|
|setOutputSide(_string_ direction)|_string_ side that was set or nil|Tries to set the output inventory side|
|setInputSide(_string_ direction)|_string_ side that was set or nil|Tries to set the input inventory side|
|getInputSide()|_string_ side or nil|Gets the input side|
|getOutputSide()|_string_ side or nil|Gets the output side|

# Player Inventory

An object that allows for manipulation of a player's inventory

## Functions

|Function|Returns|Description|
|-|-|-|
|getStackInSlot(_number_ slot)|_boolean_ success|Tries to get an entire stack from a slot|
|retrieveFromSlot(_number_ slot, _number_ amount)|_boolean_ success|Tries to get an item from the players inventory and puts it in the turtles current slot|
|pushToSlot(_number_ push_slot, _number_ pull_slot, _number_ amount)|_boolean_ success|Tries to push an item from the input side to the players inventory with the specified slot|
|push(_number_ index, _number_ amount)|_boolean_ success|Tries to push an item from the inventory on the input side|
|getSize()|_number_ size|Get the size of an inventory|
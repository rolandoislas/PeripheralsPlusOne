# Interactive Sorter

The interactive sorter is a peripheral with one inventory slot. It can provide information about the item and push and
 pull to connected inventories.
 
# Functions

|Function|Returns|Description|
|-|-|-|
|analyze()|_table_ item_info|Returns item info about the item in the slot|
|push(_string/number_ direction, _number_ amount)|_boolean_ success|Attempts to push the item to the inventory|
|pull(_string/number_ direction, _number_ amount, _number_ slot_to_attempt_to_pull_from)|_boolean_ success|Try to pull from an inventory|
|isInventoryPresent(_string/number_ direction)|_boolean_ is_present|Check if inventory is on the specified side|
# Supplied Turtle

The turtle interfaces with a [Resupply Station](/miscellaneous_additions/resupply_station.md).

# Function

|Function|Returns|Description|
|-|-|-|
|link(_string/number_ direction)|_boolean_ link_completed|Attempts to link to a resupply station|
|resupply(_number_ slot_to_pull_into, _string_ item_name, _number_ meta)|_boolean_ success|Tries to refill the current slot with the specified item|
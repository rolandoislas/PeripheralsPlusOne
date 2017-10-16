# Thirsty Turtle

The Thirsty Turtle Upgrade is a Turtle Upgrade allows Turtles to suck up and drop fluids.

## Functions
| Function | Returns | Description |
|----------|---------|-------------|
|place()|_number_ amount|Places a fluid in the world or tries to put a fluid inside a tank|
|placeUp()|_number_ amount|Places a fluid in the world or tries to put a fluid inside a tank|
|placeDown()|_number_ amount|Places a fluid in the world or tries to put a fluid inside a tank|
|suck([_number_ amount])|_number_ amount|Sucks a fluid from the world or from a tank|
|suckUp([_number_ amount])|_number_ amount|Sucks a fluid from the world or from a tank|
|suckDown([_number_ amount])|_number_ amount|Sucks a fluid from the world or from a tank|
|getFluid()|_table_ fluidContained|Gets info for a fluid contained, table has the key values: amount, id and name|
|fill([_number_ slot])|_number_ amount|Attempts to fill tge item in the turtle's current slot with fluid in the internal tank|
|drain([_number_ slot])|_number_ amount|Attempts to drain the item in the turtle's current slot and insert the contents in the internal tank|
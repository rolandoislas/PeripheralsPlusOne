# Magnetic Card Reader/Writer

The magnetic card reader/writer enables reading to and writing from a [magnetic stripe card].

# Data Format

The data format follows the [Magtek] magnetic strip card specifications.

## Quick Overview

Each magnetic card contains three data tracks that can be written to and read from. The data that can be stored on them
 is as follows:
 
|Track|Capacity|
|-----|--------|
|1|79 alphanumeric characters|
|2|40 numeric characters|
|3|107 numeric characters|

All the tracks can also utilize the following control characters: `% ^ ? ; =`.

## Functions

<table>
  <tr>
    <th>Function</th>
    <th>Returns</th>
    <th>Description</th>
  </tr>
  <tr>
    <td>write(<i>number</i> track_index, <i>string</i> data)</td>
    <td><i>nil</i></td>
    <td>Sets the buffer of data to be written to a track. Any card swiped after a track's buffer is written
     will have that track written to. The buffer(s) will remained filled until clear() is called. <b>Note: Setting
     a buffer to an empty string will clear that track on any swipped cards. <i>Nil</i> will cause that buffer to be
     ignored.</td>
  </tr>
  <tr>
    <td>clear([<i>number</i> track_index])</td>
    <td><i>nil</i></td>
    <td>Clears a specified buffer or all if one is not specified</td>
  </tr>
</table>

## Events

<table>
  <tr>
    <th>Event</th>
    <th>Return 1</th>
    <th>Return 2</th>
    <th>Return 3</th>
    <th>Return 4</th>
    <th>Description</th>
  </tr>
  <tr>
    <td>mag_swipe</td>
    <td>"mag_swipe"</td>
    <td><i>string</i> track_one</td>
    <td><i>string</i> track_two</td>
    <td><i>string</i> track_three</td>
    <td>Queued when a mag card is swiped</td>
  </tr>
</table>


[magnetic stripe card]: /miscellaneous_additions/mag_card.md
[Magtek]: https://www.magtek.com/content/documentationfiles/d99800004.pdf
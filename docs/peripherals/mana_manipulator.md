# Mana Manipulator

The Mana Manipulator allows the impossible; numbers in [Botania]! More specifically it acts as both a mana pool
 and mana spreader that can be controlled be a computer. The max capacity is 10% of a normal mana pool. It contains an
 internal inventory that can store lenses which can be equipped from Lua code. Like a normal spreader, the direction can
 also be set with a Wand of the Forest. Sparks can be used in a similar fashion to their use on a normal mana pool.
 
# Functions

<table>
  <tr>
    <th>Function</th>
    <th>Returns</th>
    <th>Description</th>
  </tr>
  <tr>
    <td>getMana([<i>boolean</i> get_real_value])</td>
    <td><i>number</i> mana_level</td>
    <td>An evil function. Returns a random value by default</td>
  </tr>
  <tr>
    <td>sendBurst()</td>
    <td><i>boolean</i> status</td>
    <td>Attempts to send a burst from the mana spreader. The burst will fail if the normal conditions for a
     spreader burst (e.g. lens allows it, there is a pool that can contain the burst) are not met.</td>
  </tr>
  <tr>
    <td>setBurstDirection(<i>number</i> rotation_x, <i>number</i> rotation_y)</td>
    <td>nil</td>
    <td>Sets the rotation of the spreader burst</td>
  </tr>
  <tr>
    <td>getBurstDirection()</td>
    <td><i>table</i> direction</td>
    <td>Gets the burst direction as a table with keys x and y</td>
  </tr>
  <tr>
    <td>setAutoBurst(<i>boolean</i> auto_burst)</td>
    <td>nil</td>
    <td>Sets the spreader to operate as a normal spreader, checking for and sending bursts automatically, or
     disabling the automatic burst.</td>
  </tr>
  <tr>
    <td>getAutoBurst()</td>
    <td><i>boolean</i> auto_burst</td>
    <td>Get the status of the auto bust functionality</td>
  </tr>
  <tr>
    <td>setVoidExcessMana(<i>boolean</i> void_excess)</td>
    <td>nil</td>
    <td>Set if an unlimited amount of mana can be accepted, but the excess voided if it is over the max storage
     limit</td>
  </tr>
  <tr>
    <td>getVoidExcessMana()</td>
    <td><i>boolean</i> void_excess</td>
    <td>Get the current status of voiding excess mana</td>
  </tr>
  <tr>
    <td>setColor(<i>number</i> r, <i>number</i> g, <i>number</i> b)</td>
    <td>nil</td>
    <td>Set the color of the bursts. Some lenses like the celebratory lens will respect this color, other will ignore
     it for their own embedded color.</td>
  </tr>
  <tr>
    <td>getColor(</td>
    <td><i>table</i> color</td>
    <td>Returns the current beam color as a table with keys r, g, and b</td>
  </tr>
  <tr>
    <td>setLens(<i>string</i> item_name, <i>number</i> meta)</td>
    <td>nil</td>
    <td>Attempts to set a lens from the internal inventory matching the provided description. The first item matched is
     set. If the lens cannot be found the function will error.</td>
  </tr>
  <tr>
      <td>setLens(<i>number</i> slot)</td>
      <td>nil</td>
      <td>Attempts to set the lens to the item stored in the specified slot. If the slot is empty, the function will
       error.</td>
    </tr>
  <tr>
    <td>getLens()</td>
    <td><i>table</i> lens</td>
    <td>Returns the current lens as a table with the following keys: id, meta, name, slot, nbt</td>
  </tr>
  <tr>
    <td>getContainedLensesLua()</td>
    <td><i>table</i> lenses</td>
    <td>Returns all contained lenses in a table. The format of each lens is the same as <i>getLens()</i>. The table
     index is not guaranteed to correcspond to the slot in which it resides.</td>
  </tr>
</table>



[Botania]: https://minecraft.curseforge.com/projects/botania
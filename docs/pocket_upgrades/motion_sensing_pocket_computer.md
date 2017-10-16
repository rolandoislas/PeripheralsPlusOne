# Motion Sending Pocket Computer

This computer sends events when a player does various actions

# Events

|Event|Return Value|Description|
|-|-|-|
|worldChanged|_number_ old_dimension, _number_ new_dimension|Fired when the player switches between dimensions|
|locationChanged|_number_ diff_x, _number_ diff_y, _number_ diff_z|Fired when the player's location changes.|
|rotationChanged|_number_ yaw, _number_ pitch|Fired when the player's rotation changes|
|blockHit||Fired when the player hits a block|
|rightClick||Fired when the player right clicks a block|
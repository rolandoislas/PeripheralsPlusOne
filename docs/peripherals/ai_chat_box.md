# AI Chat Box

The AI Chat Box allows computers to make requests to the Clever Bot API.

## Functions

|Function|Return Value|Description|
|-|-|-|
|newSession([_string_ cleverbot_api_key])|_table_ session object|Creates a new Cleverbot session|
|getSession(_string_ session_uuid)|_table_ session object|Gets a session object associated with a UUID|
|getAllSessions()|_table_ session objects|Returns all session objects|
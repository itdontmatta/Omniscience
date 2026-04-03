# Omniscience

Block logging and rollback plugin for Paper/Spigot 1.21.

## Commands

### Main Command

`/omniscience` (aliases: `/o`, `/omni`)

| Subcommand | Aliases | Description |
|---|---|---|
| `search <params>` | `s`, `sc`, `lookup`, `l` | Search data records based on lookup parameters |
| `rollback <params>` | `rb`, `roll` | Rollback changes (applies newest-first) |
| `restore <params>` | `rs`, `rst` | Restore changes (applies oldest-first) |
| `page <#>` | `p`, `pg` | Navigate to a page of search results |
| `undo` | `u` | Reverse the last rollback/restore you performed |
| `tool` | `t`, `inspect` | Toggle the Omniscience search tool |
| `events` | `e` | List all enabled, searchable events |
| `ai <question>` | `help-ai`, `ask` | Ask AI for help constructing search queries |

### Utility Command

| Command | Description |
|---|---|
| `/omnitele` | Teleport to coordinates (used by search result links) |

## Permissions

| Permission | Description |
|---|---|
| `omniscience.mayuse` | Base permission required to use the plugin |
| `omniscience.commands.search` | Use the search command |
| `omniscience.commands.rollback` | Use rollback and restore commands |
| `omniscience.commands.undo` | Use the undo command |
| `omniscience.commands.page` | Use the page command |
| `omniscience.commands.tool` | Use the search tool |
| `omniscience.commands.events` | List searchable events |
| `omniscience.commands.ai` | Use AI query assistance |
| `omniscience.commands.search.autotool` | Auto-activate search tool on join |
| `omniscience.override.maxradius` | Bypass the maximum search radius limit |

---
--- dyn.lua - PeripheralsPlusOne Package Manager
--- Author: Rolando Islas
--- Version: 0.2
--- License: GPLv2
---

local json = require("peripheralsplusone.dyn.json")

local Dyn = {}
Dyn.__index = Dyn

---
--- Main entry point.
---
--- @param args table command line arguments
--- @return table new Dyn instance
---
function Dyn:new(args)
    local dyn = {}
    setmetatable(dyn, Dyn)
    dyn.args = args
    dyn.base_path = "/.peripheralsplusone/"
    dyn.repo_file = dyn.base_path .. "sources.json"
    dyn.default_repo = "https://raw.githubusercontent.com/rolandoislas/dyn/master"
    dyn.cache_path = dyn.base_path .. "cache/"
    dyn.sources_cache_path = dyn.cache_path .. "sources/"
    dyn.install_path = dyn.base_path .. "installed/"
    return dyn
end

---
--- Parsed arguments and tries to execute a compatible command
--- Errors if anything fails
---
function Dyn:run()
    self:check_argument_count(1, "argument", false)
    local arg = self.args[1]
    if arg == "install" then
        self:check_argument_count(2, "program name")
        self:install_program(self.args[2])
    elseif arg == "remove" then
        self:check_argument_count(2, "program name")
        self:remove_program(self.args[2])
    elseif arg == "update" then
        self:check_argument_count(1, "argument")
        self:update_repo_indices()
    elseif arg == "upgrade" then
        self:check_argument_count(1, "argument")
        self:upgrade_programs()
    elseif arg == "list" then
        self:check_argument_count(1, "argument")
        self:list_programs()
    elseif arg == "search" then
        self:check_argument_count(2, "search term")
        self:search(self.args[2])
    elseif arg == "repo" then
        self:check_argument_count(2, "repo argument", false)
        local repo_arg = self.args[2]
        if repo_arg == "add" then
            self:check_argument_count(3, "repo url", false)
            if table.getn(self.args) > 4 then
                self:error("Too many arguments passed.")
            end
            self:add_repo(self.args[3], self.args[4])
        elseif repo_arg == "remove" then
            self:check_argument_count(3, "repo url/name")
        elseif repo_arg == "edit" then
            self:check_argument_count(2, "argument")
            self:show_repo_index_file()
        elseif repo_arg == "list" then
            self:check_argument_count(2, "argument")
            self:list_repos()
        else
            self:error("No repo command named \"" .. repo_arg .. "\".")
        end
    else
        self:error("No command named \"" .. arg .. "\".")
    end
end

---
--- Strip suffixes - actor, action, past tense, and plural
---
--- @param term word to strip
--- @return string string with suffices removed
---
function strip_suffix(term)
    return term:gsub("er$", ""):gsub("ing$", ""):gsub("ed$", ""):gsub("es$", ""):gsub("s$", "")
end

---
--- Search the local indices for a program name or description that contains the search term(s)
--- Prints out any found items
---
--- @param terms string search terms
---
function Dyn:search(terms)
    local repos = self:get_cached_indices()
    local matched_programs = {}
    local terms_split = self:split(terms, "%a+")
    local term_count = table.getn(terms_split)
    for repo_index, repo in pairs(repos) do
        for program_index, program in pairs(repo) do
            local name_split = self:split(program.name, "%a+")
            local description_split = self:split(program.description or "", "%a+")
            local contained_count_name = 0
            local contained_count_description = 0
            -- Iterate over the search terms and incrmet a counter if found
            for term_index, term in pairs(terms_split) do
                term = strip_suffix(term)
                -- Check if the name contains the term
                for name_index, name_part in pairs(name_split) do
                    name_part = strip_suffix(name_part)
                    if name_part == term then
                        contained_count_name = contained_count_name + 1
                        break
                    end
                end
                -- Check if the description contains the term
                for desc_index, desc_part in pairs(description_split) do
                    desc_part = strip_suffix(desc_part)
                    if desc_part == term then
                        contained_count_description = contained_count_description + 1
                        break
                    end
                end
            end
            -- Check if the counters match the amount of terms
            if term_count == contained_count_name or term_count == contained_count_description then
                table.insert(matched_programs, program)
            end
        end
    end
    local match_amount = table.getn(matched_programs);
    if match_amount > 0 then
        print(string.format("Found %d matches:", match_amount))
        for match_index, match in pairs(matched_programs) do
            self:print_program_info(match)
        end
    else
        error("No search results for: " .. terms)
    end
end

---
--- Prints the program info in a standardized format
---
--- @param program program table
---
function Dyn:print_program_info(program)
    local out = "\t%s\n\t\tVersion: %s\n\t\tDescription: %s\n\t\tSupports: %s\n\t\tDepends: %s\n"
    local depends = ""
    for _, dependency in pairs(program.depends) do
        depends = depends .. dependency .. ","
    end
    depends = depends:gsub(",$", "")
    local peripherals = ""
    for _, peripheral in pairs(program.peripherals) do
        peripherals = peripherals .. peripheral .. ","
    end
    peripherals = peripherals:gsub(",$", "")
    textutils.pagedPrint(string.format(
        out,
        program.name,
        program.version,
        program.description or "",
        peripherals,
        depends
    ), 5)
end

---
--- List all the installed programs
--- Errors if it could not get the index
---
function Dyn:list_programs()
    local installed = self:get_installed_index()
    print(string.format("Installed: %d program%s", table.getn(installed),
    table.getn(installed) == 1 and "" or "s"))
    for program_index, program in pairs(installed) do
        self:print_program_info(program)
    end
end

---
--- Check the argument count matches the bounds passed
--- Errors with a reason message.
---
--- @param index number minimun amount of arguments
--- @param name string noun to use for error messages
--- @param max boolean should the arguments end at the index passed
---
function Dyn:check_argument_count(index, name, max)
    if max == nil then
        max = true
    end
    if table.getn(self.args) < index then
        self:error("Missing " .. name .. ".")
    elseif max and table.getn(self.args) > index then
        self:error("Too many " .. name .. "s passed.")
    end
end

---
--- Initialize the sources file with defaults if it does not exist.
---
function Dyn:init_repo_file()
    if not fs.exists(self.repo_file) then
        local file_json = {
            {name="default", url=self.default_repo}
        }
        self:write_sources(file_json)
    end
end

---
--- Read and verify the sources file. If it does not exist, the file will be initialized with defaults
---
--- Format:
--- An array of objects that contain a url and name.
--- The name may not exist.
--- [{"url": "some_url", "name": "some_name}]
---
--- @return table
---
function Dyn:get_sources()
    self:init_repo_file()
    local repo_file = fs.open(self.repo_file, "r")
    if not repo_file then
        self:json_error(self.repo_file, "Could not read file.")
    end
    local success, sources = pcall(json.parse, repo_file.readAll())
    if not success then
        self:json_error(self.repo_file, sources)
    end
    repo_file.close()
    -- Verify the repo entries have their entries populated
    local names = {}
    local urls = {}
    for repo_index, repo in pairs(sources) do
        table.insert(names, repo.name)
        table.insert(urls, repo.url)
        if not repo.url then
            self:json_error(self.repo_file, "Repo missing URL.")
        elseif not repo.name then
            -- Ignore
        end
    end
    -- Verify there are no duplicates
    local duplicate = self:contains_duplicate(names)
    if duplicate then
        self:json_error(self.repo_file, "Duplicate repo name: " .. duplicate)
    end
    duplicate = self:contains_duplicate(urls)
    if duplicate then
        self:json_error(self.repo_file, "Duplicate repo url:" .. duplicate)
    end
    return sources
end

---
--- Check if a table contains a duplicate value
---
--- @param compare_table table to check for duplicates
--- @return duplicate value if found otherwise nil
---
function Dyn:contains_duplicate(compare_table)
    for item_index, item in pairs(compare_table) do
        local found_count = 0
        for item_index_compare, item_compare in pairs(compare_table) do
            if item_compare == item then
                found_count = found_count + 1
                if found_count > 1 then
                    return item
                end
            end
        end
    end
end

---
--- Write out the sources list. The sources are stringified to JSON before being written
---
--- @param sources_lua table the sources file in lua format
---
function Dyn:write_sources(sources_lua)
    local repo_file = fs.open(self.repo_file, "w")
    if not repo_file then
        self:json_error(self.repo_file, "Could not write to file.")
    end
    local success, sources = pcall(json.stringify, sources_lua)
    if not success then
        error(sources)
    end
    repo_file.write(sources)
    repo_file.close()
end

---
--- List repos in the sources list
---
function Dyn:list_repos()
    local sources = self:get_sources()
    print("Repos:")
    for repo_index, repo in pairs(sources) do
        local name = repo.name or ""
        local url = repo.url or ""
        textutils.pagedPrint(string.format(
            "\t%d\n\t\tName: %s\n\t\tUrl: %s",
            repo_index,
            name,
            url
        ), 2)
    end
end

---
--- Opens the sources list for direct editing
---
function Dyn:show_repo_index_file()
    shell.run("edit " .. self.repo_file)
end

---
--- Adds a repo to the sources list
--- Errors if the url or name has been used already.
---
--- @param url string repo root url where index.json resides
--- @param name string nilable friendly name of the repo
---
function Dyn:add_repo(url, name)
    local sources = self:get_sources()
    for repo_index, repo in pairs(sources) do
        if name and repo.name == name then
            error("Duplicate name provided for a repo: " .. name)
        end
        if repo.url == url then
            error("Duplicate url provided for a repo: " .. url)
        end
    end
    local success, message = http.checkURL(url)
    if not success then
        error(message)
    end
    table.insert(sources, {name=name, url=url})
    self:write_sources(sources)
end

---
--- Attempt to upgrade all installed programs
---
function Dyn:upgrade_programs()
    local installed = self:get_installed_index()
    local upgrade_count = 0
    for program_index, program in pairs(installed) do
        print(string.format("Checking program \"%s\" for update", program.name))
        local success, err = pcall(self.install_program, self, program.name)
        if success then
            print("Upgraded " .. program.name)
            upgrade_count = upgrade_count + 1
        else
            print(err)
        end
    end
    print(string.format("Upgraded %d program%s", upgrade_count, upgrade_count == 1 and "" or "s"))
end

---
--- Search repos for their index file, attempt to download it, and parse it.
--- Errors if any of these actions fail.
---
function Dyn:update_repo_indices()
    local sources = self:get_sources()
    for repo_index, repo in pairs(sources) do
        -- Read from url
        local repo_index_url = repo.url .. "/index.json"
        print(string.format("Downloading %d/%d %s", repo_index, table.getn(sources), repo_index_url))
        local success, message = http.checkURL(repo_index_url)
        if not success then
            error("Invalid source url: " .. repo_index_url  .. ".\n" .. message)
        end
        local repo_index_file = http.get(repo_index_url)
        if not repo_index_file then
            error("Could not access url: " .. repo_index_url)
        end
        if repo_index_file.getResponseCode() ~= 200 then
            error(string.format("Got reponse code\"%d\" for url: %s",
            repo_index_file.getResponseCode(), repo_index_url))
        end
        -- Write file
        if not fs.exists(self.cache_path) then
            fs.makeDir(self.cache_path)
        end
        if not fs.exists(self.sources_cache_path) then
            fs.makeDir(self.sources_cache_path)
        end
        local file_name = self:get_cleaned_sources_file_name(repo)
        local file_path = self.sources_cache_path .. file_name
        local source_file = fs.open(file_path, "w")
        if not source_file then
            error("Could not open file for writing: " .. file_path)
        end
        local new_source_file = repo_index_file.readAll();
        repo_index_file.close()
        local verified, err = pcall(self.verify_index_file, self, new_source_file, true)
        if not verified then
            self:json_error(repo_index_url, err)
        end
        source_file.write(new_source_file)
        source_file.close()
    end
    print("Update complete")
end

---
--- Replaces slashes, colons and periods with dashes and underscores
--- @param repo string url
--- @return string
---
function Dyn:get_cleaned_sources_file_name(repo)
    return repo.url:gsub("/", "-"):gsub(":", "-"):gsub("%.", "_")
end

---
--- Verify an index file has the correct program entries
---
--- @param file_contents string raw json string
--- @return table parsed index file
---
function Dyn:verify_index_file(file_contents, print_warnings)
    print_warnings = print_warnings == nil and false or true
    local success, source_json = pcall(json.parse, file_contents)
    if not success then
        error(source_json)
    end
    local error_message = "Program \"%s\" is missing its \"%s\" entry"
    local names = {}
    for program_index, program in pairs(source_json) do
        table.insert(names, program.name)
        if not program.name then
            error(string.format(error_message, program_index, "name"))
        end
        if not program.directory then
            error(string.format(error_message, program.name, "directory"))
        end
        if not program.peripherals or type(program.peripherals) ~= "table" then
            error(string.format(error_message, program.name, "peripherals"))
        end
        if not program.extra or type(program.extra) ~= "table" then
            error(string.format(error_message, program.name, "extra"))
        end
        if not program.version then
            error(string.format(error_message, program.name, "version"))
        elseif not dyn.parseVersion(program.version) then
            error(string.format("Program \"%s\" has an invalid version string: %s", program.name,
            program.version))
        end
        if not program.depends or type(program.depends) ~= "table" then
            error(string.format(error_message, program.name, "depends"))
        end
        if not program.description then
            program.description = ""
            if print_warnings then
                print(string.format(error_message, program.name, "description"))
            end
        end
    end
    local duplicate = self:contains_duplicate(names)
    if duplicate then
        error("Duplicate program entry found: " .. duplicate)
    end
    return source_json
end

---
--- Attempts to remove a program's files and from the index
--- Errors if program is not installed or index failed to update
---
--- @param name string program name
---
function Dyn:remove_program(name, silent)
    silent = silent or false
    local installed = self:get_installed_index()
    for program_index, program in pairs(installed) do
        if program.name == name then
            local path = self.install_path .. program.name
            self:delete(path)
            table.remove(installed, program_index)
            self:write_installed_index(installed)
            if not silent then
                print(string.format("Program \"%s\" removed.\nReattach peripherals or restart this device.", name))
            end
            return nil
        end
    end
    error(string.format("Program \"%s\" not installed.", name))
end

---
--- Delete a file or a path recursivly
---
function Dyn:delete(path)
    if not fs.exists(path) then
        return
    end
    if fs.isDir(path) then
        local list = fs.list(path)
        for _, list_path in pairs(list) do
            self:delete(path .. "/" .. list_path)
        end
    end
    fs.delete(path)
end

---
--- Reads the local indices and returns a table with each added
--- Errors if a file is missing, could not be opened for reading, or if the index format could not be verified
---
--- @return table a table filled with indices - It may be empty. They key will be the repo url
---
function Dyn:get_cached_indices()
    local sources = self:get_sources()
    local repos = {}
    for repo_index, repo in pairs(sources) do
        local file_name = self:get_cleaned_sources_file_name(repo)
        local file_path = self.sources_cache_path .. file_name
        local source_file = fs.open(file_path, "r")
        if not source_file then
            error(string.format("Could not open file %s for reading.", file_path))
        end
        local verified, source_table = pcall(self.verify_index_file, self, source_file.readAll())
        source_file.close()
        if not verified then
            self:json_error(file_path, source_table)
        end
        repos[repo.url] = source_table
    end
    return repos
end

---
--- Attempt to install a program from the current repos.
--- Errors if installed program is the lastest version.
---
--- @param name string program name
---
function Dyn:install_program(name, is_dependency)
    is_dependency = is_dependency or false
    local repos = self:get_cached_indices()
    for repo_index, repo in pairs(repos) do
        for program_index, program in pairs(repo) do
            if program.name == name then
                -- Check if there is a newer version in the repos
                local is_installed, installed_program = self:is_installed(program)
                if is_installed then
                    local success, is_newer = pcall(self.version_is_newer, self, installed_program.version,
                    program.version)
                    if not success then
                        error(string.format(
                        "Encountered error parsing \"%s\" in program index: %s",
                        installed_program.name,
                        is_newer
                        ))
                    end
                    if is_newer then
                        local removed = pcall(self.remove_program, self, name)
                        if not removed then
                            error(string.format("Failed to remove previous install of \"%s\".",
                            installed_program.name))
                        end
                    elseif not is_dependency then
                        error(string.format("Program \"%s\" is already the newest version.",
                            installed_program.name))
                    end
                end
                -- Install dependencies
                for dep_index, dep in pairs(program.depends) do
                    local dep_installed = self:is_installed(dep)
                    if not dep_installed then
                        self:install_program(dep, true)
                    end
                end
                -- Check if the program should be installed
                if not is_newer and is_installed then
                    return nil
                end
                print("Installing " .. program.name)
                -- Download the program and help text
                local files = {} -- [[path, file_data]]
                for _, extension in pairs({"lua", "txt"}) do
                    local path = string.format("%s/%s.%s", program.name, program.name,
                        extension)
                    local path_url = string.format("%s/%s.%s", program.directory, program.name,
                        extension)
                    local url = string.format("%s/%s", repo_index, path_url)
                    print("  Downloading " .. url)
                    local data = self:download_file(url)
                    if not data then
                        self:download_fail(url)
                    end
                    table.insert(files, {path, data})
                end
                -- Download the extra files
                for _, extra_file in pairs(program.extra) do
                    local path = string.format("%s/extra/%s", program.name, extra_file)
                    local path_url = string.format("%s/extra/%s", program.directory, extra_file)
                    local url = string.format("%s/%s", repo_index, path_url)
                    print("  Downloading " .. url)
                    local data = self:download_file(url)
                    if not data then
                        self:download_fail(url)
                    end
                    table.insert(files, {path, data})
                end
                -- Check and create directories
                if not fs.exists(self.install_path) then
                    fs.makeDir(self.install_path)
                end
                local extra_directory = string.format("%s%s/extra", self.install_path, program.name)
                if not fs.exists(extra_directory) then
                    fs.makeDir(extra_directory)
                end
                -- Write the downloaded files
                for _, file_table in pairs(files) do
                    local file = fs.open(self.install_path .. file_table[1], "w")
                    if not file then
                        pcall(self.remove_program, self, name)
                        error(string.format("Could not open file for writing: " .. file_table[1]))
                    end
                    file.write(file_table[2])
                    file.close()
                end
                -- Add to index
                local installed = self:get_installed_index()
                table.insert(installed, program)
                self:write_installed_index(installed)
                if not is_dependency then
                    print(string.format(
                    "Program \"%s\" installed.\nReattach peripherals or restart the device.",
                        program.name))
                end
                return nil
            end
        end
    end
    error(string.format("Could not find program \"%s\".", name))
end

---
--- Try to download a file and return the data.
--- Does not error on failure
---
--- @param url string
--- @return string downloaded file or nil on error
---
function Dyn:download_file(url)
    local valid_url, err = http.checkURL(url)
    if not valid_url then
        error(err)
    end
    local file = http.get(url)
    if not file then
        return nil
    end
    if file.getResponseCode() ~= 200 then
        file.close()
        return nil
    end
    local data = file.readAll()
    file.close()
    return data
end

---
--- Check if a program is installed. If there is no "installed index" file, false will be returned.
--- If the program is installed it is returned as the second return value.
--- A program in considered installed if the program names match. Version checking is no performed.
---
--- @param program_to_check table program table @see Dyn#verify_index_file for the expected format
--- @return boolean, table
---
function Dyn:is_installed(program_to_check)
    local installed = self:get_installed_index()
    for program_index, installed_program in pairs(installed) do
        if installed_program.name == program_to_check.name then
            return true, installed_program
        end
    end
    return false
end

---
--- Reads the installed index and creates and empty one if it does not exist
---
--- @return table installed index
---
function Dyn:get_installed_index()
    if not fs.exists(self.install_path) then
        fs.makeDir(self.install_path)
    end
    local installed_index_path = self.install_path .. "index.json"
    if not fs.exists(installed_index_path) then
        local to_init = fs.open(installed_index_path, "w")
        if not to_init then
            error("Failed to create file:" .. installed_index_path)
        end
        to_init.write("[]")
        to_init.close()
    end
    local installed_index = fs.open(installed_index_path, "r")
    if not installed_index then
        error("Could not read file: " .. installed_index_path)
    end
    local verified, installed = pcall(self.verify_index_file, self, installed_index.readAll())
    installed_index.close()
    if not verified then
        self:json_error(installed_index_path, installed)
    end
    return installed
end

---
--- Converts the passed table to JSON and writes it to the installed index file
---
--- @param index table index file as a Lua table
---
function Dyn:write_installed_index(index)
    local success, index_json = pcall(json.stringify, index)
    if not success then
        error(index_json)
    end
    local installed_index_path = self.install_path .. "index.json"
    local installed_index = fs.open(installed_index_path, "w")
    if not installed_index then
        error("Failed to open file for writing: " .. installed_index_path)
    end
    installed_index.write(index_json)
    installed_index.close()
end

---
--- Split a string into a table of the passed type
---
--- @param to_split string string to split
--- @param match string pattern to match
--- @return table
---
function Dyn:split(to_split, match)
    local t = {}
    for val in to_split:gmatch(match) do
        table.insert(t, val)
    end
    return t
end

---
--- Compare version strings in the following format: major.minor.revision-release_type-release_number
--- Each of the first for variables should be integers.
--- The release_type variable is optional and can be "alpha", "beta", or "release".
--- If not including the release type, omit the hypen and the release number.
--- Alternate formats:
---   major
---   major.minor
---   major.minor.revision
--- Example:
---   1.0.0-alpha-1
---
--- @param lesser string the version string expected to be the lesser of the two
--- @param greater string the version string expected to be the greater of the two
--- @return boolean
---
function Dyn:version_is_newer(lesser, greater)
    local lesser_version = dyn.parseVersion(lesser)
    local greater_version = dyn.parseVersion(greater)
    if not lesser_version or not greater_version then
        error("Failed to parse version string")
    end
    -- Compare versions in the format major.minor.revision.release_type.release_number
    for version_number = 1, 5 do
        local great_num = tonumber(lesser_version[version_number])
        local less_num = tonumber(greater_version[version_number])
        if great_num > less_num then
            return true
        elseif great_num < less_num then
            return false
        end
    end
    return false
end

---
--- Error with help text
---
--- @param msg string error message
---
function Dyn:error(msg)
    error(msg .. " Try \"help dyn\".")
end

---
--- Error with a message indicating a file could not be read.
---
--- @param path string file path
--- @param err string error message
---
function Dyn:json_error(path, err)
    error(
    "Error reading \"" .. path .. "\".\n" ..
    err
    )
end

---
--- Error with a download fail message
---
--- @param url string
---
function Dyn:download_fail(url)
    error("Failed to download ".. url)
end

-- Init
local dyn = Dyn:new({...})
dyn:run()
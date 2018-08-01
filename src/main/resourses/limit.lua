local key = KEYS[1]
local max_permits = tonumber(KEYS[2])
local interval_milliseconds = tonumber(KEYS[3])
local permits = tonumber(ARGV[1])

local current_permits = tonumber(redis.call("get", key) or 0)

if (current_permits + permits > max_permits) then
    return false
else
    redis.call("incrby", key, permits)
    if (current_permits == 0) then
        redis.call("pexpire", key, interval_milliseconds)
    end
    return true
end

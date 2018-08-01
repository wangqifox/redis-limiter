-- 资源唯一标识
local key = KEYS[1]
-- 时间窗口内最大并发数
local max_permits = tonumber(KEYS[2])
-- 窗口的间隔时间
local interval_milliseconds = tonumber(KEYS[3])
-- 获取的并发数
local permits = tonumber(ARGV[1])

local current_permits = tonumber(redis.call("get", key) or 0)

-- 如果超过了最大并发数，返回false
if (current_permits + permits > max_permits) then
    return false
else
    -- 增加并发计数
    redis.call("incrby", key, permits)
    -- 如果key中保存的并发计数为0，说明当前是一个新的时间窗口，它的过期时间设置为窗口的过期时间
    if (current_permits == 0) then
        redis.call("pexpire", key, interval_milliseconds)
    end
    return true
end

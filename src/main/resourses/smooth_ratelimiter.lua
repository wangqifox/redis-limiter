-- key
local key = KEYS[1]
-- 最大存储的令牌数
local max_permits = tonumber(KEYS[2])
-- 每秒钟产生的令牌数
local permits_per_second = tonumber(KEYS[3])
-- 请求的令牌数
local required_permits = tonumber(ARGV[1])

-- 下次请求可以获取令牌的起始时间
local next_free_ticket_micros = tonumber(redis.call('hget', key, 'next_free_ticket_micros') or 0)

-- 当前时间
local time = redis.call('time')
local now_micros = tonumber(time[1]) * 1000000 + tonumber(time[2])

-- 查询获取令牌是否超时
if (ARGV[2] ~= nil) then
    -- 获取令牌的超时时间
    local timeout_micros = tonumber(ARGV[2])
    local micros_to_wait = next_free_ticket_micros - now_micros
    if (micros_to_wait > timeout_micros) then
        return micros_to_wait
    end
end

-- 当前存储的令牌数
local stored_permits = tonumber(redis.call('hget', key, 'stored_permits') or 0)
-- 添加令牌的时间间隔
local stable_interval_micros = 1000000 / permits_per_second

-- 补充令牌
if (now_micros > next_free_ticket_micros) then
    local new_permits = (now_micros - next_free_ticket_micros) / stable_interval_micros
    stored_permits = math.min(max_permits, stored_permits + new_permits)
    next_free_ticket_micros = now_micros
end

-- 消耗令牌
local moment_available = next_free_ticket_micros
local stored_permits_to_spend = math.min(required_permits, stored_permits)
local fresh_permits = required_permits - stored_permits_to_spend;
local wait_micros = fresh_permits * stable_interval_micros

redis.replicate_commands()
redis.call('hset', key, 'stored_permits', stored_permits - stored_permits_to_spend)
redis.call('hset', key, 'next_free_ticket_micros', next_free_ticket_micros + wait_micros)
redis.call('expire', key, 10)

-- 返回需要等待的时间长度
return moment_available - now_micros

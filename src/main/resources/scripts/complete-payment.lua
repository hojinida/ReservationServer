local idempotencyKey = KEYS[1]
local seatKey = KEYS[2]
local resultMessage = ARGV[1]
local ttl = ARGV[2]

local existing = redis.call('GET', idempotencyKey)
if existing then
    return existing
end

redis.call('PERSIST', seatKey)

redis.call('SET', idempotencyKey, resultMessage, 'EX', ttl)

return resultMessage

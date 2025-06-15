local idempotencyKey = KEYS[1]
local seatKey = KEYS[2]
local userShardKey = KEYS[3]
local userId = ARGV[1]
local resultMessage = ARGV[2]
local ttl = ARGV[3]

local existing = redis.call('GET', idempotencyKey)
if existing then
    return existing
end

redis.call('HMSET', seatKey,
    'isReserved', 'false',
    'reservedBy', '',
    'reservedAt', ''
)
redis.call('PERSIST', seatKey)

redis.call('SREM', userShardKey, userId)

redis.call('SET', idempotencyKey, resultMessage, 'EX', ttl)

return resultMessage

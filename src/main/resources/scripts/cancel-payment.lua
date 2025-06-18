local seatKey = KEYS[1]
local userShardKey = KEYS[2]
local userId = ARGV[1]

redis.call('HMSET', seatKey,
    'isReserved', 'false',
    'reservedBy', '',
    'reservedAt', ''
)

redis.call('PERSIST', seatKey)

redis.call('SREM', userShardKey, userId)

return resultMessage

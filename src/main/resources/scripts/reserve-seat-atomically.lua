local userSetKey = KEYS[1]
local seatKey = KEYS[2]
local userId = ARGV[1]
local lockTime = ARGV[2]

local amount = redis.call('HGET', seatKey, 'amount')
if not amount then
  return {-2, 0}
end

if redis.call('SISMEMBER', userSetKey, userId) == 1 then
  return {-1, 0}
end

local isReserved = redis.call('HGET', seatKey, 'isReserved')
if isReserved == 'true' then
  return {0, 0}
end

redis.call('HMSET', seatKey,
  'isReserved', 'true',
  'reservedBy', userId,
  'reservedAt', redis.call('TIME')[1]
)
redis.call('PEXPIRE', seatKey, lockTime)
redis.call('SADD', userSetKey, userId)

return {1, tonumber(amount)}

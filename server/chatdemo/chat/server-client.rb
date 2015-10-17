module ServerClient

=begin
  EVENT
  {
    event: 'on_new_room',
    room: room
  }
  PUSH
  {
    'event': 'on_new_room',
    'room_id': 'room_id_1'
  }
=end
  def on_new_room(ws, channels, user_id, subscriptions, data)

    # suscribe to new channel
    room_id = data[:room].id.to_s
    chan = channels[room_id]
    sid = chan.subscribe do |data|
      eval "#{data[:event]}(ws, channels, user_id, subscriptions, data)"
    end
    subscriptions << [chan, sid]

    # push to client
    ws.send({
      event: 'on_new_room',
      room_id: room_id
    }.to_json)
  end

  def on_leave(ws, channels, user_id, subscriptions, data)
  end

=begin
  EVENT
  {
    event: 'on_new_message',
    message: message
  }
  PUSH
  {
    'event': 'on_new_message',
    'id': 'message_id_1',
    'body': 'Hello There!!!',
    'time': 1444986700,
    'room_id': 'room_id_1',
    'user_id': 'user_id_1'
  }
=end
  def on_new_message(ws, channels, user_id, subscriptions, data)
    ws.send({
      event: 'on_new_message',
      id: data[:message].id.to_s,
      body: data[:message].body,
      time: data[:message].time,
      room_id: data[:message].room_id.to_s,
      user_id: data[:message].user_id.to_s
    }.to_json)
  end
end
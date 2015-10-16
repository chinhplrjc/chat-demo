module ServerClient

=begin
  EVENT
  {
    to: ['current_user_id', ...],
    event: 'on_join',
    room: room
  }
  PUSH
  {
    'event': 'on_join',
    'room_id': 'room_id_1'
  }
=end
  def on_join(ws, channels, user_id, subscriptions, data)

    # suscribe to new channel
    room_id = data[:room].id.to_s
    chan = channels[room_id]
    sid = chan.subscribe do |data|
      eval "#{data[:event]}(ws, channels, user_id, subscriptions, data)"
    end
    subscriptions << [chan, sid]

    # push to client
    ws.send({
      event: 'on_join',
      room_id: room_id
    }.to_json)
  end

  def on_leave
  end

  def on_msg

  end
end
module ClientServer

  ERR_INVALID_USER_ID = 100


  # TODO: validate data for all actions

=begin
  REQ
  {
    'action': 'login',
    'user_id': 'user_id_1'
  }
  RES
  {
    err: 0,
    rooms: [
      {
        'id': 'room_id_1',
        'name': 'Room 1'
        'users': ['user_id_1', 'user_id_2']
      },
      ...
    ]
  }
=end
  def login(ws, channels, user_id, subscriptions, json)
    user = User.where(user_id: json['user_id']).first
    if user
      user_id[0] = json['user_id']
      user.rooms.each do |r|
        chan = channels[r.id.to_s]
        next if !chan
        sid = chan.subscribe do |data|
          eval "#{data[:event]}(ws, channels, user_id, subscriptions, data)"
        end
        subscriptions << [chan, sid]
      end
      ws.send({
        err: 0,
        rooms: user.rooms.map do |r|
          {
            id: r.id.to_s,
            name: r.name,
            users: r.users.map { |u| u.id.to_s }
          }
        end
      }.to_json)
    else
      ws.send({err: ERR_INVALID_USER_ID}.to_json)
    end
  end

=begin
  REQ
  {
    'action': 'create_room',
    'users': ['user_id_1', 'user_id_2', 'user_id_3'],
    'name': "Room 1"
  }
  RES
  {
    'err': 0,
    'id': 'room_id_100'
  }
=end
  def create_room(ws, channels, user_id, subscriptions, json)

    # create room
    room = Room.new
    room.name = json['name']
    room.users = User.where(user_id: {:$in => json['users'] | user_id}).to_a
    room.save

    # create channel for room
    channels[room.id.to_s] = EM::Channel.new

    # push event to all users
    channels[:all].push({
      to: json['users'],
      event: 'on_join',
      room: room
    })

    # response
    ws.send({
      err: 0,
      id: room.id.to_s
    }.to_json)
  end

=begin
  REQ
  {
    'action': 'get_room',
    'room_id': 'room_id_1'
  }
  RES
  {
    'err': 0,
    'id': 'room_id_1',
    'name': 'Room 1'
    'users': ['user_id_1', 'user_id_2']
  }
=end
  def get_room(ws, channels, user_id, subscriptions, json)
    room = Room.where(id: json['room_id']).first
    if room
      ws.send({
        err: 0,
        id: room.id.to_s,
        name: room.name,
        users: room.users.map { |u| u.id.to_s }
      }.to_json)
    else
      ws.send({err: ERR_INVALID_ROOM_ID}.to_json)
    end
  end

=begin
  REQ
  {
    'action': 'create_message',
    'room_id': 'room_id_1',
    'body': "Hello there!!!"
  }
  RES
  {
    'err': 0,
    'id': 'message_id_1'
  }
=end
  def create_message(ws, channels, user_id, subscriptions, json)

    # create message
    message = Message.new
    message.body = json['body']
    message.time = Time.now.to_i
    message.room_id = json['room_id']
    message.user_id = user_id[0]
    message.save

    # push to other users
    chan = channels[json['room_id']]
    chan.push({
      event: 'on_message',
      message: message
    })

    # response
    ws.send({
      err: 0,
      id: message.id.to_s
    }.to_json)
  end

=begin
  REQ
  {
    'action': 'get_room_messages',
    'room_id': 'room_id_1'
  }
  RES
  {
    'err': 0,
    'messages': [
      {
        'id': 'message_id_1',
        'body': 'Hello There!!!',
        'time': 1444986700,
        'room_id': 'room_id_1',
        'user_id': 'user_id_1'
      }
    ]
  }
=end
  def get_room_messages(ws, channels, user_id, subscriptions, json)
    messages = Message.where(room_id: json['room_id']).to_a
    ws.send({
      err: 0,
      messages: messages.map do |m|
        {
          id: m.id.to_s,
          body: m.body,
          time: m.time,
          room_id: m.room_id.to_s,
          user_id: m.user_id.to_s
        }
      end
    }.to_json)
  end
end
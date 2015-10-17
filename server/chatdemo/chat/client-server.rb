module ClientServer

  # TODO: validate data for all actions
  # TODO: authenticate all actions (check if use has logged in or not)

=begin
  REQ
  {
    'tag': 123,
    'action': 'login',
    'user_id': 'user_id_1'
  }
  RES
  {
    'tag': 123,
    'err': 0,
    'rooms': [
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
        tag: json['tag'],
        err: 0,
        rooms: user.rooms.map do |r|
          {
            id: r.id.to_s,
            name: r.name,
            users: r.users.map { |u| u.user_id }
          }
        end
      }.to_json)
    else
      ws.send({
        tag: json['tag'],
        err: ERR_INVALID_USER_ID
      }.to_json)
    end
  end

=begin
  REQ
  {
    'tag': 123,
    'action': 'create_room',
    'users': ['user_id_1', 'user_id_2', 'user_id_3'],
    'name': "Room 1"
  }
  RES
  {
    'tag': 123,
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
      to: json['users'] | user_id,
      event: 'on_new_room',
      room: room
    })

    # response
    ws.send({
      tag: json['tag'],
      err: 0,
      id: room.id.to_s
    }.to_json)
  end

=begin
  REQ
  {
    'tag': 123,
    'action': 'get_room',
    'room_id': 'room_id_1'
  }
  RES
  {
    'tag': 123,
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
        tag: json['tag'],
        err: 0,
        id: room.id.to_s,
        name: room.name,
        users: room.users.map { |u| u.user_id }
      }.to_json)
    else
      ws.send({
        tag: json['tag'],
        err: ERR_INVALID_ROOM_ID
      }.to_json)
    end
  end

=begin
  REQ
  {
    'tag': 123,
    'action': 'create_message',
    'room_id': 'room_id_1',
    'body': "Hello there!!!"
  }
  RES
  {
    'tag': 123,
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
      event: 'on_new_message',
      message: message
    })

    # response
    ws.send({
      tag: json['tag'],
      err: 0,
      id: message.id.to_s
    }.to_json)
  end

=begin
  REQ
  {
    'tag': 123,
    'action': 'get_room_messages',
    'room_id': 'room_id_1',
    'limit': 20
  }
  RES
  {
    'tag': 123,
    'err': 0,
    'messages': [
      {
        'id': 'message_id_1',
        'body': 'Hello There!!!',
        'time': 1444986700,
        'room_id': 'room_id_1',
        'user_id': 'user_id_1'
      },
      ...
    ]
  }
=end
  def get_room_messages(ws, channels, user_id, subscriptions, json)
    crit = Message.where(room_id: json['room_id']).order_by(:time.desc)
    if json['limit'] && json['limit'] > 0
      crit = crit.limit(json['limit'])
    end
    messages = crit.to_a
    ws.send({
      tag: json['tag'],
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
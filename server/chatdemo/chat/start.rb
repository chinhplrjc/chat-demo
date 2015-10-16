#!/usr/bin/env ruby
require 'em-websocket'
require 'mongoid'
require 'json'
require 'require_all'

# errors
require_all 'config/initializers/errors.rb'

# modules
require_all 'chat/client-server.rb'
require_all 'chat/server-client.rb'
include ClientServer
include ServerClient

# mongoid
Mongoid.load!("config/mongoid.yml", :development)
require_all 'app/models'

EventMachine.run do

  # create channels for all rooms
  channels = {}
  Room.all.to_a.each do |r|
    channels[r.id.to_s] = EM::Channel.new
  end

  # create common channel for all users
  channels[:all] = EM::Channel.new

  EventMachine::WebSocket.start(host: "0.0.0.0", port: 8080, debug: true) do |ws|

    ws.onopen do |handshake|

      # declare variables for each websocket connection
      user_id = []
      subscriptions = []

      # subscribe to common channel
      common_channel_sid = channels[:all].subscribe do |data|
        if data[:to].include?(user_id[0])
          eval "#{data[:event]}(ws, channels, user_id, subscriptions, data)"
        end
      end

      ws.onmessage do |msg|
        begin
          json = JSON.parse(msg)
          eval "#{json['action']}(ws, channels, user_id, subscriptions, json)"
        rescue => e
          puts e
          ws.send({ err: ERR_BAD_REQUEST }.to_json)
        end
        
      end

      ws.onclose do |event|
        # unsubscribe all channels
        subscriptions.each do |chan, sid|
          chan.unsubscribe(sid)
        end

        # unsubscribe common channal
        channels[:all].unsubscribe(common_channel_sid)
      end
    end
  end
end
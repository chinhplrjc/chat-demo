class Message
  include Mongoid::Document

  field :body, type: String, default: ''
  field :time, type: Integer

  belongs_to :room, class_name: 'Room', inverse_of: :messages
  belongs_to :user, class_name: 'User'
end
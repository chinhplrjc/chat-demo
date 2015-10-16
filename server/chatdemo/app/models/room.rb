class Room
  include Mongoid::Document

  field :name, type: String, default: ''

  has_many :messages, class_name: 'Message', inverse_of: :room
  has_and_belongs_to_many :users, class_name: 'User'
end
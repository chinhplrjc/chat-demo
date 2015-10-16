class User
  include Mongoid::Document

  field :user_id, type: String, default: ''

  has_and_belongs_to_many :rooms, class_name: 'Room'
end
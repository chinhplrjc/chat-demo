class ApiController < ActionController::Base

  def register
    user_id = params[:user_id]

    # check if user exists
    if User.where(user_id: user_id).count > 0
      render json: { err: ERR_USER_ALREADY_EXIST }
      return
    end

    # create user
    u = User.new
    u.user_id = user_id
    u.save

    # response
    render json: { err: 0 }
  end

  def get_all_users
    render json: {
      err: 0,
      users: User.all.to_a.map do |u|
        {
          user_id: u.user_id
        }
      end
    }
  end
end
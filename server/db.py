import datetime
from sqlalchemy import create_engine

from sqlalchemy.orm import sessionmaker, relationship
from sqlalchemy import Column, Integer, String, ForeignKey
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.types import Boolean, DateTime, Enum

# DB classes

engine = create_engine('sqlite:///chat-battle.db')
Base = declarative_base()


class Message(Base):
    __tablename__ = 'messages'

    id = Column(Integer, primary_key=True)
    text = Column(String)
    time = Column(DateTime)
    whiteboard_tag = Column(String)

    chat_id = Column(Integer, ForeignKey('chats.id'))
    author_id = Column(Integer, ForeignKey('players.id'))

    def to_dict(self):
        return {"id": self.id,
                "text": self.text,
                "whiteboardTag": self.whiteboard_tag,
                "authorId": self.author_id,
                "time": str(self.time),
                "chatId": str(self.chat_id)}

    @staticmethod
    def from_dict(message_dict):
        return Message(id=message_dict["id"], text=message_dict["text"],
                       chat_id=message_dict["chatId"],
                       author_id=message_dict["authorId"],
                       whiteboard_tag=message_dict["whiteboardTag"],
                       time=datetime.datetime.strptime(message_dict["time"],
                                                       '%Y-%m-%d %H:%M:%S.%f'))


class Player(Base):
    __tablename__ = 'players'

    id = Column(Integer, primary_key=True)
    login = Column(String, nullable=False)
    password_hash = Column(String)
    chat_id = Column(Integer, ForeignKey('chats.id'))
    status = Column(Enum('IDLE', 'CHATTING_AS_PLAYER', 'CHATTING_AS_LEADER',
                         'IN_QUEUE_AS_LEADER', 'IN_QUEUE_AS_PLAYER'))
    penalty = Column(Enum('NONE', 'MUTED', 'KICKED'))
    mute_end_time = Column(DateTime)
    rating = Column(Integer)
    email = Column(String)

    messages = relationship("Message", backref="sender")

    def to_dict(self):
        return {"id": self.id,
                "login": self.login,
                "chatId": self.chat_id,
                "status": self.status,
                "penalty": self.penalty,
                "rating": self.rating}

    @staticmethod
    def from_dict(player_dict):
        return Player(id=player_dict["id"], login=player_dict["login"],
                      chat_id=player_dict["chatId"],
                      status=player_dict["status"],
                      rating=player_dict["rating"])


class Chat(Base):
    __tablename__ = 'chats'

    id = Column(Integer, primary_key=True)
    creation_time = Column(DateTime)
    type = Column(Integer)

    is_started = Column(Boolean, default=False)
    is_closed = Column(Boolean)
    leader_id = Column(Integer)
    winner_id = Column(Integer)
    accepted = Column(Integer, default=0)
    end_time = Column(DateTime)

    messages = relationship("Message", backref="chat")
    players = relationship("Player", backref="chat")

    def to_dict(self):
        return {"id": self.id,
                "creation_time": self.creation_time,
                "type": self.type,
                "is_started": self.is_started,
                "is_closed": self.is_closed,
                "leader_id": self.leader_id,
                "winner_id": self.winner_id,
                "accepted": self.accepted}

    @staticmethod
    def from_dict(chat_dict):
        return Chat(id=chat_dict["id"],
                    creation_time=datetime.datetime.strptime(chat_dict["time"],
                                                    '%Y-%m-%d %H:%M:%S.%f'),
                    type=chat_dict["type"],
                    is_started=chat_dict["is_started"],
                    is_closed=chat_dict["is_closed"],
                    leader_id=chat_dict["leader_id"],
                    winner_id=chat_dict["leader_id"],
                    accepted=chat_dict["accepted"])

Base.metadata.create_all(engine)

Session = sessionmaker(bind=engine)
session = Session()

import datetime
from sqlalchemy import create_engine

from sqlalchemy.orm import sessionmaker, relationship
from sqlalchemy import Column, Integer, String, ForeignKey
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.sql import text
from sqlalchemy.types import Boolean, DateTime


# DB classes

engine = create_engine('sqlite:///chat-battle.db')
Base = declarative_base()


class Message(Base):
    __tablename__ = 'messages'

    id = Column(Integer, primary_key=True)
    text = Column(String, nullable=False)
    time = Column(DateTime)

    chat_id = Column(Integer, ForeignKey('chats.id'))
    author_id = Column(Integer, ForeignKey('players.id'))

    def toDict(self):
        return {"id": self.id,
                "text": self.text,
                "authorId": self.author_id,
                "time": str(self.time)}

    @staticmethod
    def fromDict(message_dict):
        return Message(id=message_dict["id"], text=message_dict["text"],
                       author_id=message_dict["authorId"],
                       time=datetime.datetime.strptime(message_dict["time"],
                                                       '%Y-%m-%d %H:%M:%S.%f'))


class Player(Base):
    __tablename__ = 'players'

    id = Column(Integer, primary_key=True)
    login = Column(String, nullable=False)
    age = Column(Integer)
    password_hash = Column(String)
    sex = Column(String)  # 'MALE'/'FEMALE'
    chat_id = Column(Integer, ForeignKey('chats.id'))
    status = Column(String)   

    messages = relationship("Message", backref="sender")
    # chats = relationship("Chat", backref="leader") # TODO: not chats but chat

    def toDict(self):
        return {"id": self.id,
                "login": self.login,
                "sex": self.sex,
                "age": self.age,
                "chatId": self.chat_id,
                "status": self.status}

    @staticmethod
    def fromDict(player_dict):
        player = session.query(Player).filter_by(id=message_dict["id"]).first()
        return Player(id=message_dict["id"], login=message_dict["login"],
                      sex=message_dict["sex"], age=message_dict["age"],
                      chat_id=message_dict["chatId"], status=message_dict["status"])


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


    messages = relationship("Message", backref="chat")
    players = relationship("Player", backref="chat")

    def toDict(self):
        return {"id": self.id,
                "creation_time": self.creation_time,
                "type": self.type,
                "is_started": self.is_started,
                "is_closed": self.is_closed,
                "leader_id": self.leader_id,
                "winner_id": self.winner_id,
                "accepted": self.accepted}

Base.metadata.create_all(engine)

Session = sessionmaker(bind=engine)
session = Session()

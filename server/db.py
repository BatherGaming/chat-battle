import sqlalchemy
import datetime
from sqlalchemy import create_engine

from sqlalchemy.orm import sessionmaker, relationship
from sqlalchemy import Column, Integer, String, Float, ForeignKey
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.sql import text


# DB classes

engine = create_engine('sqlite:///chat-battle.db')
Base = declarative_base()

class Message(Base):
    __tablename__ = 'messages'

    id = Column(Integer, primary_key=True)
    text = Column(String, nullable=False)
    time = Column(sqlalchemy.types.DateTime)
    
    chat_id = Column(Integer, ForeignKey('chats.id'))
    author_id = Column(Integer, ForeignKey('players.id'))

   

    def map_repr(self):
        return {
            "id": self.id,
            "text": self.text,
            "author_id": self.author_id,
            "time": str(self.time)
        }

class Player(Base):
    __tablename__ = 'players'

    id = Column(Integer, primary_key=True)
    login = Column(String)
    age = Column(Integer)
    password_hash = Column(String)
    sex = Column(String)  # 'male'/'female'
    chat_id = Column(Integer, ForeignKey('chats.id'))

    messages = relationship("Message", backref="sender")


    def map_repr(self):
        return {"id": self.id,
                "login": self.login,
                "sex": self.sex,
                "age": self.age,
                "chatId": self.chat_id}



class Chat(Base):
    __tablename__ = 'chats'

    id = Column(Integer, primary_key=True)
    creation_time = Column(sqlalchemy.types.DateTime)
    type = Column(Integer)
    is_closed = Column(sqlalchemy.types.Boolean)

    messages = relationship("Message", backref="chat")
    players = relationship("Player", backref="chat")

Base.metadata.create_all(engine)

Session = sessionmaker(bind=engine)
session = Session()

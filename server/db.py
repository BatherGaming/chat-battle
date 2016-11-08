from sqlalchemy import create_engine

from sqlalchemy.orm import sessionmaker
from sqlalchemy import Column, Integer, String
from sqlalchemy.ext.declarative import declarative_base


# DB classes

engine = create_engine('sqlite:///chat-battle.db')
Base = declarative_base()

class Player(Base):
    __tablename__ = 'players'

    id = Column(Integer, primary_key=True)
    login = Column(String)
    age = Column(Integer)
    password_hash = Column(String)
    sex = Column(String) # 'male'/'female'

    def map_repr(self):
        return {"id": self.id,
                "login": self.login,
                "sex": self.sex,
                "age": self.age}



Base.metadata.create_all(engine)

Session = sessionmaker(bind=engine)
session = Session()

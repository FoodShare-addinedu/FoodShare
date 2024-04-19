import React, {useEffect, useState} from 'react';
import axios from 'axios';
import {jwtDecode} from 'jwt-decode';
import {Link} from "react-router-dom";
import './ChatRoomList.scss'

function ChatRoomList() {
    const [rooms, setRooms] = useState([]);
    const [error, setError] = useState('');


    const fetchRooms = () => {
        const token = localStorage.getItem('token');
        if (!token) {
            setError('로그인이 필요합니다.');
            return;
        }
        const decoded = jwtDecode(token);
        const userId = decoded.uid;
        axios.get('/api/ListRooms', {
            headers: {
                'Authorization': `Bearer ${token}`
            },
            params: {
                userId: userId
            }
        })
            .then(response => {
                setRooms(response.data);
            })
            .catch(error => {
                console.error('채팅방 목록을 가져오는데 실패했습니다.', error);
                setError('채팅방 목록을 가져오는데 실패했습니다.');
            });
    };


    useEffect(() => {
        fetchRooms();
    }, []);


    const hideRoom = (chatRoomId) => {
        const token = localStorage.getItem('token');
        const decoded = jwtDecode(token);
        const userId = decoded.uid;

        axios.put(`/api/hideRoom/${chatRoomId}`, null, {
            params: {
                userId: userId
            },
            headers: {
                'Authorization': `Bearer ${token}`
            }
        }).then(() => {
            fetchRooms();
        }).catch(error => {
            console.error('채팅방 숨기기에 실패했습니다.', error);
        });
    };


    if (error) {
        return <div>오류: {error}</div>;
    }


    return (
        <div className="chat-room-list-container">
            <h1>채팅방 목록</h1>
            <ul className="chat-room-list">
                {rooms.map(chatRoom => (
                    <li key={chatRoom.chatRoomId} className="chat-room">
                        <Link to={`/chat/GetChat/${chatRoom.chatRoomId}`} className="chat-room-link">
                            <img src="/images/userImage.png" alt="User" className="chat-room-image"/>
                            <div className="chat-room-info">
                                <p className="chat-room-name">{chatRoom.chatRoomId}</p>
                                <p className="chat-room-last-message">{chatRoom.lastMessage}</p>
                                <p className="chat-room-time">{new Date(chatRoom.lastMessageTimestamp).toLocaleString()}</p>
                            </div>
                        </Link>
                        <button id="deleteButton" onClick={() => hideRoom(chatRoom.chatRoomId)}>채팅방 삭제</button>
                    </li>
                ))}
            </ul>
        </div>
    );

}

export default ChatRoomList;
import './MainDetail.scss';
import { Avatar } from "rsuite";
import {useEffect, useState} from "react";
import axios from "axios";
import {NavLink, useParams} from "react-router-dom";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faHeart} from "@fortawesome/free-regular-svg-icons";
import Caution from "../../../components/caution/Caution";
import CautionData from "../../../data/CautionData";
import MapView from "../../../components/address/MapView";
import {IconButton, SquareButton} from "../../../components/button/Button";
import {faAngleRight, faArrowRight} from "@fortawesome/free-solid-svg-icons";
import Drawers from "../../../components/drawer/Drawers";

import { formatDistanceToNow, parseISO } from 'date-fns';
import { ko } from 'date-fns/locale';
import ChatButton from "../../Chat/ChatButton";

export default function MainDetail() {
    const {id} = useParams();
    const [food, setFood] = useState({ giver: {} }); // 초기 상태에 giver 객체 포함
    const [isFavorited, setIsFavorited] = useState(false); // 초기값은 false로 설정
    const [favoriteCount, setFavoriteCount] = useState(food.likes || 0); // 찜 횟수 초기화

    console.log(isFavorited);

    const fullScreenMapStyle = {
        width: '100vw',
        height: '100vh',
        position: 'fixed',
        top: 0,
        left: 0,
        zIndex: 1050 // Drawer z-index보다 높거나 같게 설정
    };

    useEffect(() => {
        const fetchFoodsData = async () => {
            try {
                const response = await axios.get(`/api/foods/${id}`, {
                    headers: {
                        'Authorization': `Bearer ${localStorage.getItem('jwt')}`
                    },

                });
                setFood(response.data);
                setIsFavorited(response.data.isFavorite);
                setFavoriteCount(response.data.likes);
                console.log(response.data);
            } catch (error) {
                console.error('Failed to fetch data:', error);
            }
        };

        fetchFoodsData();
    }, [id]);

    function formatTimeAgo(dateStr) {
        console.log('Date String:', dateStr); // Check what you actually receive as input

        if (!dateStr) {
            console.error('Invalid or undefined date string');
            return 'Date unavailable'; // Or some default/fallback value
        }
        const date = parseISO(dateStr);  // 서버에서 'yyyy-MM-dd' 형식의 문자열로 받은 날짜를 Date 객체로 변환
        return formatDistanceToNow(date, { addSuffix: true, locale: ko });  // 현재 시간과의 차이를 자연스럽게 표현
    }

    const toggleFavorite = async () => {
        try {
            const method = isFavorited ? 'delete' : 'post'; // 현재 찜 상태에 따라 메서드 결정
            const response = await axios({
                method: method,
                url: `/api/favorites/${food.foodId}`,
                data: {
                    // userId: food.giver.userId, // 현재 로그인한 사용자 ID
                    isFavorite: !isFavorited
                },
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('jwt')}`
                }
            });
            setIsFavorited(!isFavorited); // 찜 상태 토글
            setFavoriteCount(prev => isFavorited ? prev - 1 : prev + 1); // 찜 횟수 조정
            console.log(isFavorited);
            console.log(response);

        } catch (error) {
            console.error('Error toggling favorite:', error);
        }
    };

    if (!food || !food.giver || !food.giver.mobileNumber) {
        return <div>Loading...</div>; // 데이터가 로드되기를 기다리는 동안 로딩 표시
    }



    console.log("giver.mobileNumber",food.giver.mobileNumber);

    return (
        <section className={'main_detail'}>
            <div className={'img_wrap'}>
                {food.imageUrls  &&
                    <img src={food.imageUrls[0]} alt={food.title}/>
                }
            </div>
            <div className={'content_wrap'}>
                <div className={'user_wrap'}>
                    <Avatar className={'avatar'} circle/>
                    <div className={'user_info'}>
                        {/*<p className={'nick_name'}>{food.giver.nickname} 닉네임 자리입니다만</p>*/}
                        <p className={'location'}>{food.location}</p>
                        <p className={'nick_name'}>{food.nickname} 닉네임 자리입니다만</p>
                    </div>
                </div>
                <div className={'title_wrap'}>
                    <h5>{food.title}</h5>
                    {food.createdAt && <p className={'date'}>{formatTimeAgo(food.createdAt)}</p>}
                    {/*<p className={'date'}>{food.createdAt}1시간전</p>*/}
                </div>
                <div className={'dates_wrap'}>
                    <p><span>소비기한</span>{food.eatByDate}</p>
                </div>
                <div className={'description_wrap'}>
                    <p>{food.description}</p>
                </div>
                <div className={'caution_wrap'}>
                    <Caution items={CautionData}/>
                </div>
                <div className={'map_wrap'}>
                    <div className={'title_wrap'}>
                        <h6>나눔 희망장소</h6>
                        <FontAwesomeIcon icon={faAngleRight} />
                    </div>
                    <MapView/>
                </div>
                {/*<Drawers*/}
                {/*    trigger={*/}
                {/*        <div className={'map_wrap'}>*/}
                {/*            <div className={'title_wrap'}>*/}
                {/*                <h6>나눔 희망장소</h6>*/}
                {/*                <FontAwesomeIcon icon={faAngleRight} />*/}
                {/*            </div>*/}
                {/*            <MapView/>*/}
                {/*        </div>*/}
                {/*    }*/}
                {/*    drawerContent={<MapView zoomable={true} draggable={true}/>}*/}
                {/*/>*/}
                <div className={'actions_wrap'}>
                    <IconButton
                        icon={faHeart}
                        onClick={toggleFavorite}
                        style={{ color: isFavorited ? 'red' : 'grey' }}
                    />
                    {/*<NavLink to={`/chat/${food.foodId}/${food.giver.userId}`}>*/}
                    <NavLink to={`/chat/${food.foodId}`}>
                        <SquareButton name={'채팅하기'} />
                    </NavLink>
                    <section className={'main_detail'}>
                        <div className={'actions_wrap'}>
                            <ChatButton foodId={food.foodId} giverId={food.giver.mobileNumber} />
                        </div>
                    </section>
                </div>
            </div>
        </section>
    )
}
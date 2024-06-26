import './Main.scss';
import Cards from "../../../components/card/Cards";
import {NavLink} from "react-router-dom";
import axios from "axios";
import React, {useEffect, useRef, useState} from "react";
import {Tabs} from "rsuite";
import {AddButton} from "../../../components/button/Button";

export default function Main() {
    const [foodData, setFoodData] = useState([]);
    const [localData, setLocalData] = useState([]);
    const [loading, setLoading] = useState(false);
    const [hasMore, setHasMore] = useState(true);
    const [page, setPage] = useState(0); // 현재 페이지 번호를 상태로 관리
    const [panel, setPanel] = useState([
        { key: '1', title: '나눔거래' },
        { key: '2', title: '나의동네' }
    ]);

    useEffect(() => {
        fetchFoodsData();  // 컴포넌트가 마운트될 때 초기 데이터 로드
    }, []);

    const loader = useRef(null);

    const fetchFoodsData = async () => {
        if(!hasMore || loading) return;
        console.log('데이터 요청 시작...');

        setLoading(true);
        try{
            const response = await axios.get('/api/foods', {
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('jwt')}`
                },
                params: { page, size: 5 } // 현재 페이지 번호를 파라미터로 전달
            });

            if(response.data.content.length === 0){
                setHasMore(false);
            }else{
                setFoodData(prev => [...prev, ...response.data.content]);
                setPage(prevPage => prevPage + 1); // 다음 페이지 번호로 업데이트
            }
        }catch (error){
            console.log('Falied to fetch data:', error);
        }finally {
            setLoading(false);
        }
    }


    return (
        <section className={'main'}>
            <Tabs className={'main_tab'} defaultActiveKey="1" appearance="subtle">
                {panel.map((tab) => (
                    <Tabs.Tab className={'tab'} eventKey={tab.key} title={tab.title} key={tab.key}>
                        <div className={'tab_content'}>
                            {loading && <div>Loading...</div>}
                            {tab.key === '1' ?
                                foodData.map((food, index) => (
                                    <NavLink to={`foods/${food.foodId}`} key={`${food.foodId}-${index}`}>
                                        <Cards food={food}/>
                                    </NavLink>
                                )) : '나랑 시켜먹을래?'}
                            {!hasMore && <div>No more data available.</div>}
                            {hasMore && !loading && (
                                <button onClick={fetchFoodsData}>더보기</button>
                            )}
                        </div>
                    </Tabs.Tab>
                ))}
            </Tabs>
            <NavLink to={'/main/add'}>
                <AddButton/>
            </NavLink>
        </section>
    );
}
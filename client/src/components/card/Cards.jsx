import {Box, Card, Flex, Text} from '@radix-ui/themes';
export default function Cards({food}) {
    return(
        <Flex gap="3" direction="column">
            <Box width="350px">
                <Card size="1">
                    <Flex gap="3" align="center">
                        <img
                            src={food.imageUrls[0]}
                            alt={food.title}
                            style={{
                                display: 'block',
                                objectFit: 'cover',
                                width: '100%',
                                height: 140,
                                backgroundColor: 'var(--gray-5)',
                            }}
                        />
                        <Box>
                            <Text as="div" size="2" weight="bold">
                                {food.title}
                            </Text>
                            <Text as="div" size="2" color="gray">
                                {food.description}
                            </Text>
                        </Box>
                    </Flex>
                </Card>
            </Box>
        </Flex>
    )
}
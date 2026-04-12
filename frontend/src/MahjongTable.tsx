import React, { useRef, useEffect, useState } from 'react';
import * as THREE from 'three';
import { OrbitControls } from 'three/addons/controls/OrbitControls.js';
import CHARS_1 from './assets/tiles/1_CHARS.jpg';
import CHARS_2 from './assets/tiles/2_CHARS.jpg';
import CHARS_3 from './assets/tiles/3_CHARS.jpg';
import CHARS_4 from './assets/tiles/4_CHARS.jpg';
import CHARS_5 from './assets/tiles/5_CHARS.jpg';
import CHARS_6 from './assets/tiles/6_CHARS.jpg';
import CHARS_7 from './assets/tiles/7_CHARS.jpg';
import CHARS_8 from './assets/tiles/8_CHARS.jpg';
import CHARS_9 from './assets/tiles/9_CHARS.jpg';
import BALLS_1 from './assets/tiles/1_BALLS.jpg';
import BALLS_2 from './assets/tiles/2_BALLS.jpg';
import BALLS_3 from './assets/tiles/3_BALLS.jpg';
import BALLS_4 from './assets/tiles/4_BALLS.jpg';
import BALLS_5 from './assets/tiles/5_BALLS.jpg';
import BALLS_6 from './assets/tiles/6_BALLS.jpg';
import BALLS_7 from './assets/tiles/7_BALLS.jpg';
import BALLS_8 from './assets/tiles/8_BALLS.jpg';
import BALLS_9 from './assets/tiles/9_BALLS.jpg';
import STICKS_1 from './assets/tiles/1_STICKS.jpg';
import STICKS_2 from './assets/tiles/2_STICKS.jpg';
import STICKS_3 from './assets/tiles/3_STICKS.jpg';
import STICKS_4 from './assets/tiles/4_STICKS.jpg';
import STICKS_5 from './assets/tiles/5_STICKS.jpg';
import STICKS_6 from './assets/tiles/6_STICKS.jpg';
import STICKS_7 from './assets/tiles/7_STICKS.jpg';
import STICKS_8 from './assets/tiles/8_STICKS.jpg';
import STICKS_9 from './assets/tiles/9_STICKS.jpg';
import FLOWERS from './assets/tiles/FLOWER.jpg';

interface Tile {
    number: number;
    type: string;
    isFlower: boolean;
}

const TILE_IMAGES: Record<string, string> = {
    'CHARS_1': CHARS_1, 'CHARS_2': CHARS_2, 'CHARS_3': CHARS_3,
    'CHARS_4': CHARS_4, 'CHARS_5': CHARS_5, 'CHARS_6': CHARS_6,
    'CHARS_7': CHARS_7, 'CHARS_8': CHARS_8, 'CHARS_9': CHARS_9,
    'BALLS_1': BALLS_1, 'BALLS_2': BALLS_2, 'BALLS_3': BALLS_3,
    'BALLS_4': BALLS_4, 'BALLS_5': BALLS_5, 'BALLS_6': BALLS_6,
    'BALLS_7': BALLS_7, 'BALLS_8': BALLS_8, 'BALLS_9': BALLS_9,
    'STICKS_1': STICKS_1, 'STICKS_2': STICKS_2, 'STICKS_3': STICKS_3,
    'STICKS_4': STICKS_4, 'STICKS_5': STICKS_5, 'STICKS_6': STICKS_6,
    'STICKS_7': STICKS_7, 'STICKS_8': STICKS_8, 'STICKS_9': STICKS_9,
    'FLOWERS_1': FLOWERS,
};

const CLAIM_REQUIRED: Record<string, number> = { chow: 2, pong: 2, kang: 3 };

const MahjongTable = () => {
    const mountRef = useRef<HTMLDivElement>(null);
    const [currentHand, setCurrentHand] = useState<Tile[]>([]);
    const [selectedTile, setSelectedTile] = useState<{ type: string, number: number } | null>(null);
    const [gameId, setGameId] = useState<number>(0);
    const [currentPlayerTurn, setCurrentPlayerTurn] = useState(1);
    const [moves, setMoves] = useState<string[]>([]);

    // Last discarded tile state
    const [lastDiscardedTile, setLastDiscardedTile] = useState<Tile | null>(null);
    const [lastDiscardTileSelected, setLastDiscardTileSelected] = useState(false);
    const [claimAction, setClaimAction] = useState<'chow' | 'pong' | 'kang' | null>(null);
    const [claimSelectedIndices, setClaimSelectedIndices] = useState<number[]>([]);

    // Refs for Three.js objects
    const sceneRef = useRef<THREE.Scene | null>(null);
    const cameraRef = useRef<THREE.PerspectiveCamera | null>(null);
    const rendererRef = useRef<THREE.WebGLRenderer | null>(null);
    const controlsRef = useRef<OrbitControls | null>(null);
    const playerTilesRef = useRef<THREE.Group[]>([]);
    const animationIdRef = useRef<number | null>(null);
    // const discardedTilesRef = useRef<THREE.Group[]>([]);
    const lastDiscardTileRef = useRef<THREE.Group | null>(null);

    // Get the hand from the API
    useEffect(() => {
        const fetchHand = async () => {
            try {
                const response = await fetch('/api/game/new');
                const result = await response.json();
                setCurrentHand(result.data.currentPlayerHand);
                setGameId(result.data.gameId);
            } catch (error) {
                console.error('Error fetching hand:', error);
                setCurrentHand([
                    { number: 1, type: 'CHARS', isFlower: false },
                    { number: 2, type: 'CHARS', isFlower: false },
                    { number: 3, type: 'CHARS', isFlower: false },
                    { number: 4, type: 'CHARS', isFlower: false },
                    { number: 5, type: 'CHARS', isFlower: false },
                    { number: 6, type: 'CHARS', isFlower: false },
                    { number: 7, type: 'CHARS', isFlower: false },
                    { number: 8, type: 'CHARS', isFlower: false },
                    { number: 9, type: 'CHARS', isFlower: false },
                    { number: 1, type: 'BALLS', isFlower: false },
                    { number: 2, type: 'BALLS', isFlower: false },
                    { number: 3, type: 'BALLS', isFlower: false },
                    { number: 4, type: 'BALLS', isFlower: false },
                ]);
            }
        };
        fetchHand();
    }, []);

    // Initialize Three.js scene once
    useEffect(() => {
        if (!mountRef.current) return;

        const width = mountRef.current.clientWidth;
        const height = mountRef.current.clientHeight;

        const scene = new THREE.Scene();
        scene.background = new THREE.Color(0x1a4d2e);
        sceneRef.current = scene;

        const camera = new THREE.PerspectiveCamera(60, width / height, 0.1, 1000);
        camera.position.set(0, 6, 18);
        camera.lookAt(0, 0, 0);
        cameraRef.current = camera;

        const renderer = new THREE.WebGLRenderer({ antialias: true });
        renderer.setSize(width, height);
        renderer.shadowMap.enabled = true;
        renderer.shadowMap.type = THREE.PCFSoftShadowMap;
        mountRef.current.appendChild(renderer.domElement);
        rendererRef.current = renderer;

        const controls = new OrbitControls(camera, renderer.domElement);
        controls.target.set(0, 0, 0);
        controls.enableDamping = true;
        controls.dampingFactor = 0.05;
        controls.minDistance = 8;
        controls.maxDistance = 30;
        controls.minPolarAngle = Math.PI / 6;
        controls.maxPolarAngle = Math.PI / 2.5;
        controls.enableRotate = true;
        controls.enablePan = false;
        controls.mouseButtons = {
            LEFT: THREE.MOUSE.ROTATE,
            MIDDLE: THREE.MOUSE.DOLLY,
            RIGHT: THREE.MOUSE.ROTATE
        };
        controls.touches = {
            ONE: THREE.TOUCH.ROTATE,
            TWO: THREE.TOUCH.DOLLY_ROTATE
        };
        controlsRef.current = controls;

        const ambientLight = new THREE.AmbientLight(0xffffff, 0.6);
        scene.add(ambientLight);

        const directionalLight = new THREE.DirectionalLight(0xffffff, 0.8);
        directionalLight.position.set(5, 15, 7);
        directionalLight.castShadow = true;
        directionalLight.shadow.camera.near = 0.1;
        directionalLight.shadow.camera.far = 50;
        directionalLight.shadow.camera.left = -20;
        directionalLight.shadow.camera.right = 20;
        directionalLight.shadow.camera.top = 20;
        directionalLight.shadow.camera.bottom = -20;
        scene.add(directionalLight);

        const outerRadius = 15;
        const innerRadius = 4;
        const tableShape = new THREE.Shape();
        tableShape.absarc(0, 0, outerRadius, 0, Math.PI * 2, false);
        const holePath = new THREE.Path();
        holePath.absarc(0, 0, innerRadius, 0, Math.PI * 2, true);
        tableShape.holes.push(holePath);
        const tableGeometry = new THREE.ExtrudeGeometry(tableShape, { depth: 0.3, bevelEnabled: false });
        tableGeometry.rotateX(-Math.PI / 2);
        const tableMaterial = new THREE.MeshStandardMaterial({ color: 0x2d5a3d, roughness: 0.7, metalness: 0.1 });
        const table = new THREE.Mesh(tableGeometry, tableMaterial);
        table.position.y = -0.15;
        table.receiveShadow = true;
        scene.add(table);

        const centerGeometry = new THREE.CircleGeometry(innerRadius, 32);
        centerGeometry.rotateX(-Math.PI / 2);
        const centerMaterial = new THREE.MeshStandardMaterial({ color: 0x3d6d4d, roughness: 0.6, metalness: 0.1 });
        const centerArea = new THREE.Mesh(centerGeometry, centerMaterial);
        centerArea.position.y = -0.14;
        centerArea.receiveShadow = true;
        scene.add(centerArea);

        const wallGeometry = new THREE.BoxGeometry(3, 0.4, 3);
        const wallMaterial = new THREE.MeshStandardMaterial({ color: 0xd4c4b0, roughness: 0.4, metalness: 0.1 });
        const wallPile = new THREE.Mesh(wallGeometry, wallMaterial);
        wallPile.position.set(0, 0.2, 0);
        wallPile.castShadow = true;
        wallPile.receiveShadow = true;
        scene.add(wallPile);

        createOpponentHand(Math.PI / 2, 8, scene);
        createOpponentHand(Math.PI, 8, scene);
        createOpponentHand(-Math.PI / 2, 8, scene);

        const animate = () => {
            animationIdRef.current = requestAnimationFrame(animate);
            playerTilesRef.current.forEach((tile, index) => {
                tile.position.y = 0.6 + Math.sin(Date.now() * 0.003 + index) * 0.05;
            });
            if (controlsRef.current) controlsRef.current.update();
            renderer.render(scene, camera);
        };
        animate();

        const raycaster = new THREE.Raycaster();
        const mouse = new THREE.Vector2();
        let hoverIndex = -1;

        const onMouseMove = (event: MouseEvent) => {
            if (!mountRef.current) return;
            const rect = mountRef.current.getBoundingClientRect();
            mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1;
            mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1;

            raycaster.setFromCamera(mouse, camera);

            // Check last discarded tile for cursor hint
            if (lastDiscardTileRef.current) {
                const discardHits = raycaster.intersectObjects([lastDiscardTileRef.current], true);
                if (discardHits.length > 0) {
                    document.body.style.cursor = 'pointer';
                    return;
                }
            }

            const intersects = raycaster.intersectObjects(playerTilesRef.current, true);
            if (intersects.length > 0) {
                const intersectedObject = intersects[0].object;
                const tile = intersectedObject.parent;
                if (tile && playerTilesRef.current.includes(tile as THREE.Group)) {
                    hoverIndex = playerTilesRef.current.indexOf(tile as THREE.Group);
                    document.body.style.cursor = 'pointer';
                    playerTilesRef.current.forEach((t, i) => {
                        if (i === hoverIndex) t.position.y = 0.9;
                    });
                } else {
                    hoverIndex = -1;
                    document.body.style.cursor = 'default';
                }
            } else {
                hoverIndex = -1;
                document.body.style.cursor = 'default';
            }
        };

        const onClick = (event: MouseEvent) => {
            if (!mountRef.current) return;
            const rect = mountRef.current.getBoundingClientRect();
            mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1;
            mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1;

            raycaster.setFromCamera(mouse, camera);

            // Check last discarded tile first
            if (lastDiscardTileRef.current) {
                const discardHits = raycaster.intersectObjects([lastDiscardTileRef.current], true);
                if (discardHits.length > 0) {
                    setLastDiscardTileSelected(true);
                    return;
                }
            }

            // Check player hand tiles
            const intersects = raycaster.intersectObjects(playerTilesRef.current, true);
            if (intersects.length > 0) {
                const intersectedObject = intersects[0].object;
                const tile = intersectedObject.parent;
                if (tile && playerTilesRef.current.includes(tile as THREE.Group)) {
                    setSelectedTile(tile.userData as { type: string; number: number });
                    setLastDiscardTileSelected(false);
                }
            }
        };

        const currentMount = mountRef.current;
        currentMount.addEventListener('mousemove', onMouseMove);
        currentMount.addEventListener('click', onClick);

        const handleResize = () => {
            if (!mountRef.current) return;
            const w = mountRef.current.clientWidth;
            const h = mountRef.current.clientHeight;
            camera.aspect = w / h;
            camera.updateProjectionMatrix();
            renderer.setSize(w, h);
        };
        window.addEventListener('resize', handleResize);

        return () => {
            if (animationIdRef.current) cancelAnimationFrame(animationIdRef.current);
            window.removeEventListener('resize', handleResize);
            currentMount.removeEventListener('mousemove', onMouseMove);
            currentMount.removeEventListener('click', onClick);
            document.body.style.cursor = 'default';
            if (currentMount && renderer.domElement && currentMount.contains(renderer.domElement)) {
                currentMount.removeChild(renderer.domElement);
            }
            renderer.dispose();
            if (controlsRef.current) controlsRef.current.dispose();
            scene.traverse((object) => {
                if ((object as THREE.Mesh).isMesh) {
                    const mesh = object as THREE.Mesh;
                    mesh.geometry?.dispose();
                    if (Array.isArray(mesh.material)) {
                        mesh.material.forEach(m => m.dispose());
                    } else if (mesh.material) {
                        mesh.material.dispose();
                    }
                }
            });
        };
    }, []);

    const getTileImage = (number: number, type: string) => {
        const key = `${type}_${number}`;
        return TILE_IMAGES[key] || FLOWERS;
    };

    // Update player hand when currentHand changes
    useEffect(() => {
        if (!sceneRef.current || currentHand.length === 0) return;

        playerTilesRef.current.forEach(tile => {
            sceneRef.current?.remove(tile);
            tile.traverse((child) => {
                if ((child as THREE.Mesh).isMesh) {
                    const mesh = child as THREE.Mesh;
                    mesh.geometry?.dispose();
                    if (Array.isArray(mesh.material)) {
                        mesh.material.forEach(m => m.dispose());
                    } else if (mesh.material) {
                        mesh.material.dispose();
                    }
                }
            });
        });

        const tiles: THREE.Group[] = [];
        const angle = 0;
        const distance = 8;
        const tileSpacing = 0.9;

        currentHand.forEach((handTile, i) => {
            const tile = createMahjongTile(handTile.number, handTile.type);
            const offsetX = (i - currentHand.length / 2) * tileSpacing + 0.45;
            const x = Math.sin(angle) * distance + Math.cos(angle) * offsetX;
            const z = Math.cos(angle) * distance - Math.sin(angle) * offsetX;
            tile.position.set(x, 0.6, z);
            tile.rotation.y = -angle;
            tile.rotation.x = -0.2;
            sceneRef.current?.add(tile);
            tile.name = `${handTile.type}_${handTile.number}`;
            tile.userData = { type: handTile.type, number: handTile.number };
            tiles.push(tile);
        });

        playerTilesRef.current = tiles;
    }, [currentHand]);

    // Sync 3D last discard tile when state changes
    useEffect(() => {
        if (!sceneRef.current) return;

        // Remove old discard tile
        if (lastDiscardTileRef.current) {
            sceneRef.current.remove(lastDiscardTileRef.current);
            lastDiscardTileRef.current.traverse((child) => {
                if ((child as THREE.Mesh).isMesh) {
                    const mesh = child as THREE.Mesh;
                    mesh.geometry?.dispose();
                    if (Array.isArray(mesh.material)) {
                        mesh.material.forEach(m => m.dispose());
                    } else if (mesh.material) {
                        mesh.material.dispose();
                    }
                }
            });
            lastDiscardTileRef.current = null;
        }

        if (!lastDiscardedTile) return;

        // Place discarded tile upright in the center area, facing the player
        const tile = createMahjongTile(lastDiscardedTile.number, lastDiscardedTile.type, 0xffe8cc);
        tile.position.set(0, 0.6, 2.2);
        tile.rotation.x = -0.1; // slight tilt toward player, same feel as hand tiles
        tile.userData = { isLastDiscard: true };
        sceneRef.current.add(tile);
        lastDiscardTileRef.current = tile;
    }, [lastDiscardedTile]);

    // Helper: Create opponent hand
    const createOpponentHand = (angle: number, distance: number, scene: THREE.Scene) => {
        const handSize = 13;
        const tileSpacing = 0.9;
        for (let i = 0; i < handSize; i++) {
            const tile = createMahjongTile();
            const offsetX = (i - handSize / 2) * tileSpacing + 0.45;
            const x = Math.sin(angle) * distance + Math.cos(angle) * offsetX;
            const z = Math.cos(angle) * distance - Math.sin(angle) * offsetX;
            tile.position.set(x, 0.6, z);
            tile.rotation.y = -angle;
            tile.rotation.x = -0.2;
            tile.scale.set(0.7, 0.7, 0.7);
            tile.traverse((child) => {
                if ((child as THREE.Mesh).isMesh) {
                    const mesh = child as THREE.Mesh;
                    if (mesh.material) {
                        const material = (mesh.material as THREE.Material).clone();
                        material.transparent = true;
                        material.opacity = 0.4;
                        mesh.material = material;
                    }
                }
            });
            scene.add(tile);
        }
    };

    // Helper: Create mahjong tile
    const createMahjongTile = (number?: number, type?: string, color: number = 0xf5e6d3) => {
        const group = new THREE.Group();
        const tileGeometry = new THREE.BoxGeometry(0.8, 1.2, 0.5);
        const tileMaterial = new THREE.MeshStandardMaterial({ color, roughness: 0.3, metalness: 0.1 });
        const tile = new THREE.Mesh(tileGeometry, tileMaterial);
        tile.castShadow = true;
        tile.receiveShadow = true;
        tile.userData = { number, type };
        group.add(tile);

        const imageUrl = number && type ? getTileImage(number, type) : '';
        const textureLoader = new THREE.TextureLoader();
        textureLoader.load(
            imageUrl,
            (texture) => {
                const faceMaterial = new THREE.MeshStandardMaterial({ map: texture, roughness: 0.3, metalness: 0.1 });
                const faceGeometry = new THREE.PlaneGeometry(0.78, 1.18);
                const face = new THREE.Mesh(faceGeometry, faceMaterial);
                face.position.z = 0.251;
                group.add(face);
            },
            undefined,
            () => {
                const fallbackMaterial = new THREE.MeshStandardMaterial({ color: 0xcccccc, roughness: 0.3, metalness: 0.1 });
                const faceGeometry = new THREE.PlaneGeometry(0.78, 1.18);
                const face = new THREE.Mesh(faceGeometry, fallbackMaterial);
                face.position.z = 0.251;
                group.add(face);
            }
        );
        return group;
    };

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const syncStateFromResponse = (data: any) => {
        setCurrentHand(data.currentPlayerHand);
        setCurrentPlayerTurn(data.currentPlayerTurn);
        if (data.moves) setMoves(data.moves);
        const pile: Tile[] = data.discardedTiles ?? [];
        const last = pile.length > 0 ? pile[pile.length - 1] : null;
        setLastDiscardedTile(last);
        setLastDiscardTileSelected(false);
    };

    const handleDrawTile = async () => {
        try {
            const response = await fetch(`/api/game/drawTile?gameId=${gameId}&playerId=1`);
            const result = await response.json();
            syncStateFromResponse(result.data);
        } catch (error) {
            console.error('Error drawing tile:', error);
        }
    };

    const handleDiscard = async () => {
        if (selectedTile === null) {
            alert('Please select a tile first');
            return;
        }
        try {
            const response = await fetch(`/api/game/discardTile?gameId=${gameId}&playerId=1`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ type: selectedTile.type, number: selectedTile.number })
            });
            const result = await response.json();
            setSelectedTile(null);
            syncStateFromResponse(result.data);
        } catch (error) {
            console.error('Error discarding tile:', error);
        }
    };

    const handleSortTiles = async () => {
        try {
            const response = await fetch(`api/game/sortTiles?gameId=${gameId}&playerId=1`);
            const result = await response.json();
            syncStateFromResponse(result.data);
        } catch (error) {
            console.error('Error sorting tile:', error);
        }
    };

    const handleExchangeFlowers = async () => {
        try {
            const response = await fetch(`api/game/exchangeFlowers?gameId=${gameId}&playerId=1`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
            });
            const result = await response.json();
            syncStateFromResponse(result.data);
        } catch (error) {
            console.error('Error exchanging flowers:', error);
        }
    };

    const handleAdvanceToNextTurn = async () => {
        try {
            const response = await fetch(`api/game/computerTurn?gameId=${gameId}&playerId=${currentPlayerTurn}`);
            const result = await response.json();
            syncStateFromResponse(result.data);
        } catch (error) {
            console.error('Error advancing to next turn:', error);
        }
    };

    const handleOpenClaim = (action: 'chow' | 'pong' | 'kang') => {
        setClaimAction(action);
        setClaimSelectedIndices([]);
    };

    const toggleClaimTile = (index: number) => {
        const required = CLAIM_REQUIRED[claimAction!];
        setClaimSelectedIndices(prev => {
            if (prev.includes(index)) return prev.filter(i => i !== index);
            if (prev.length >= required) return prev;
            return [...prev, index];
        });
    };

    const handleClaimOk = async () => {
        if (!claimAction || !lastDiscardedTile) return;
        const selectedTiles = claimSelectedIndices.map(i => currentHand[i]);
        try {
            const endpoint = claimAction === 'kang' ? 'kang' : claimAction;
            const response = await fetch(`/api/game/${endpoint}?gameId=${gameId}&playerId=1`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    selectedTile: lastDiscardedTile,
                    tiles: selectedTiles,
                }),
            });
            const result = await response.json();
            setCurrentHand(result.data.currentPlayerHand);
            setCurrentPlayerTurn(result.data.currentPlayerTurn);
            setMoves(result.data.moves);
            setLastDiscardedTile(null);
            setLastDiscardTileSelected(false);
        } catch (error) {
            console.error(`Error claiming ${claimAction}:`, error);
        } finally {
            setClaimAction(null);
            setClaimSelectedIndices([]);
        }
    };

    const btnBase: React.CSSProperties = {
        padding: '1rem 2rem',
        fontSize: '1rem',
        fontWeight: 'bold',
        color: 'white',
        border: 'none',
        borderRadius: '0.5rem',
        cursor: 'pointer',
        boxShadow: '0 4px 6px rgba(0, 0, 0, 0.3)',
        transition: 'all 0.2s',
    };

    const claimRequired = claimAction ? CLAIM_REQUIRED[claimAction] : 0;
    const claimReady = claimSelectedIndices.length === claimRequired;

    return (
        <div className="w-screen h-screen m-0 p-0 overflow-hidden bg-gray-900">
            <div ref={mountRef} style={{ width: '100%', height: '100%' }} />

            {/* Info panel */}
            <div className="h-80 flex flex-col gap-10 max-w-96" style={{
                position: 'absolute', top: '1rem', left: '1rem',
                color: 'white', backgroundColor: 'rgba(0,0,0,0.5)',
                padding: '1rem', borderRadius: '0.5rem',
                fontFamily: 'system-ui, -apple-system, sans-serif'
            }}>
                <section>
                    <h2 style={{ fontSize: '1.25rem', fontWeight: 'bold', marginBottom: '0.5rem' }}>Mahjong Table</h2>
                    <p style={{ fontSize: '0.875rem' }}>Tiles in hand: {currentHand.length}</p>
                    <p style={{ fontSize: '0.75rem', color: '#d1d5db', marginTop: '0.5rem' }}>Click tiles to select</p>
                    {lastDiscardedTile && (
                        <p style={{ fontSize: '0.75rem', color: lastDiscardTileSelected ? '#fbbf24' : '#86efac', marginTop: '0.25rem' }}>
                            Last discard: {lastDiscardedTile.type} {lastDiscardedTile.number}
                            {lastDiscardTileSelected ? ' ✓ selected' : ' — click it to claim'}
                        </p>
                    )}
                </section>
                <section className="history__container">
                    <h2>History</h2>
                    <ul className="max-h-30 overflow-y-auto">
                        {moves.map((move, i) => (
                            <li key={i} style={{ fontSize: '0.75rem', color: '#d1d5db' }}>{move}</li>
                        ))}
                    </ul>
                </section>
            </div>

            {/* Action buttons */}
            <div style={{
                position: 'relative', bottom: '9rem', left: '50%',
                transform: 'translateX(-50%)', display: 'flex', gap: '1rem', zIndex: 1000, flexWrap: 'wrap',
                justifyContent: 'center',
            }}>
                <button style={{ ...btnBase, backgroundColor: '#984fb9' }} onClick={handleSortTiles}>
                    Sort tiles
                </button>
                <button style={{ ...btnBase, backgroundColor: '#c29810' }} onClick={handleExchangeFlowers}>
                    Exchange flowers
                </button>
                <button style={{ ...btnBase, backgroundColor: '#3b82f6' }} onClick={handleDrawTile}
                    onMouseEnter={e => e.currentTarget.style.backgroundColor = '#2563eb'}
                    onMouseLeave={e => e.currentTarget.style.backgroundColor = '#3b82f6'}>
                    Draw Tile
                </button>
                <button
                    onClick={handleDiscard}
                    disabled={selectedTile === null}
                    style={{
                        ...btnBase,
                        backgroundColor: selectedTile === null ? '#6b7280' : '#ef4444',
                        cursor: selectedTile === null ? 'not-allowed' : 'pointer',
                        opacity: selectedTile === null ? 0.5 : 1,
                    }}
                    onMouseEnter={e => { if (selectedTile !== null) e.currentTarget.style.backgroundColor = '#dc2626'; }}
                    onMouseLeave={e => { if (selectedTile !== null) e.currentTarget.style.backgroundColor = '#ef4444'; }}
                >
                    Discard
                </button>
                <button
                    onClick={() => handleOpenClaim('chow')}
                    disabled={!lastDiscardTileSelected}
                    style={{
                        ...btnBase,
                        backgroundColor: lastDiscardTileSelected ? '#10b981' : '#6b7280',
                        cursor: lastDiscardTileSelected ? 'pointer' : 'not-allowed',
                        opacity: lastDiscardTileSelected ? 1 : 0.5,
                    }}
                    onMouseEnter={e => { if (lastDiscardTileSelected) e.currentTarget.style.backgroundColor = '#059669'; }}
                    onMouseLeave={e => { if (lastDiscardTileSelected) e.currentTarget.style.backgroundColor = '#10b981'; }}
                >
                    Chow
                </button>
                <button
                    onClick={() => handleOpenClaim('pong')}
                    disabled={!lastDiscardTileSelected}
                    style={{
                        ...btnBase,
                        backgroundColor: lastDiscardTileSelected ? '#f59e0b' : '#6b7280',
                        cursor: lastDiscardTileSelected ? 'pointer' : 'not-allowed',
                        opacity: lastDiscardTileSelected ? 1 : 0.5,
                    }}
                    onMouseEnter={e => { if (lastDiscardTileSelected) e.currentTarget.style.backgroundColor = '#d97706'; }}
                    onMouseLeave={e => { if (lastDiscardTileSelected) e.currentTarget.style.backgroundColor = '#f59e0b'; }}
                >
                    Pong
                </button>
                <button
                    onClick={() => handleOpenClaim('kang')}
                    disabled={!lastDiscardTileSelected}
                    style={{
                        ...btnBase,
                        backgroundColor: lastDiscardTileSelected ? '#8b5cf6' : '#6b7280',
                        cursor: lastDiscardTileSelected ? 'pointer' : 'not-allowed',
                        opacity: lastDiscardTileSelected ? 1 : 0.5,
                    }}
                    onMouseEnter={e => { if (lastDiscardTileSelected) e.currentTarget.style.backgroundColor = '#7c3aed'; }}
                    onMouseLeave={e => { if (lastDiscardTileSelected) e.currentTarget.style.backgroundColor = '#8b5cf6'; }}
                >
                    Kang
                </button>
                <button
                    onClick={handleAdvanceToNextTurn}
                    disabled={currentPlayerTurn === 1}
                    style={{
                        ...btnBase,
                        backgroundColor: currentPlayerTurn !== 1 ? '#f59e0b' : '#6b7280',
                        cursor: currentPlayerTurn !== 1 ? 'pointer' : 'not-allowed',
                        opacity: currentPlayerTurn !== 1 ? 1 : 0.5,
                    }}
                    onMouseEnter={e => { if (currentPlayerTurn !== 1) e.currentTarget.style.backgroundColor = '#d97706'; }}
                    onMouseLeave={e => { if (currentPlayerTurn !== 1) e.currentTarget.style.backgroundColor = '#f59e0b'; }}
                >
                    Advance to next turn
                </button>
            </div>

            {/* Claim modal */}
            {claimAction && (
                <div style={{
                    position: 'fixed', inset: 0, backgroundColor: 'rgba(0,0,0,0.75)',
                    display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 2000,
                }}>
                    <div style={{
                        backgroundColor: '#1f2937', borderRadius: '1rem', padding: '2rem',
                        maxWidth: '680px', width: '90%', color: 'white',
                        boxShadow: '0 20px 60px rgba(0,0,0,0.5)',
                    }}>
                        <h2 style={{ fontSize: '1.5rem', fontWeight: 'bold', marginBottom: '0.5rem', textTransform: 'capitalize' }}>
                            {claimAction}
                        </h2>
                        <p style={{ color: '#9ca3af', marginBottom: '1.5rem', fontSize: '0.875rem' }}>
                            Select {claimRequired} tile{claimRequired !== 1 ? 's' : ''} from your hand to complete the{' '}
                            {claimAction} with{' '}
                            <strong style={{ color: '#fbbf24' }}>
                                {lastDiscardedTile?.type} {lastDiscardedTile?.number}
                            </strong>.
                            ({claimSelectedIndices.length}/{claimRequired} selected)
                        </p>

                        {/* Discarded tile preview */}
                        <div style={{ display: 'flex', gap: '1rem', marginBottom: '1.5rem', alignItems: 'center' }}>
                            <div style={{ textAlign: 'center' }}>
                                <p style={{ fontSize: '0.75rem', color: '#9ca3af', marginBottom: '0.5rem' }}>Discarded tile</p>
                                {lastDiscardedTile && (
                                    <img
                                        src={getTileImage(lastDiscardedTile.number, lastDiscardedTile.type)}
                                        alt={`${lastDiscardedTile.type} ${lastDiscardedTile.number}`}
                                        style={{
                                            width: '56px', height: '80px', objectFit: 'cover',
                                            borderRadius: '4px', border: '2px solid #fbbf24',
                                        }}
                                    />
                                )}
                            </div>
                            <div style={{ fontSize: '1.5rem', color: '#6b7280' }}>+</div>
                            <div>
                                <p style={{ fontSize: '0.75rem', color: '#9ca3af', marginBottom: '0.5rem' }}>
                                    Your selection ({claimSelectedIndices.length}/{claimRequired})
                                </p>
                                <div style={{ display: 'flex', gap: '0.25rem', minHeight: '80px', alignItems: 'center' }}>
                                    {claimSelectedIndices.map(i => (
                                        <img
                                            key={i}
                                            src={getTileImage(currentHand[i].number, currentHand[i].type)}
                                            alt={`${currentHand[i].type} ${currentHand[i].number}`}
                                            style={{
                                                width: '56px', height: '80px', objectFit: 'cover',
                                                borderRadius: '4px', border: '2px solid #10b981',
                                            }}
                                        />
                                    ))}
                                    {Array.from({ length: claimRequired - claimSelectedIndices.length }).map((_, i) => (
                                        <div key={i} style={{
                                            width: '56px', height: '80px', borderRadius: '4px',
                                            border: '2px dashed #4b5563', backgroundColor: '#374151',
                                        }} />
                                    ))}
                                </div>
                            </div>
                        </div>

                        {/* Hand tiles */}
                        <p style={{ fontSize: '0.75rem', color: '#9ca3af', marginBottom: '0.75rem' }}>Your hand — click to select:</p>
                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem', marginBottom: '2rem' }}>
                            {currentHand.map((tile, i) => {
                                const isSelected = claimSelectedIndices.includes(i);
                                const isDisabled = !isSelected && claimSelectedIndices.length >= claimRequired;
                                return (
                                    <div key={i} onClick={() => !isDisabled && toggleClaimTile(i)} style={{ cursor: isDisabled ? 'default' : 'pointer' }}>
                                        <img
                                            src={getTileImage(tile.number, tile.type)}
                                            alt={`${tile.type} ${tile.number}`}
                                            style={{
                                                width: '48px', height: '68px', objectFit: 'cover',
                                                borderRadius: '4px',
                                                border: isSelected ? '2px solid #10b981' : '2px solid transparent',
                                                opacity: isDisabled ? 0.35 : 1,
                                                transform: isSelected ? 'translateY(-6px)' : 'none',
                                                transition: 'transform 0.1s, opacity 0.1s',
                                                boxShadow: isSelected ? '0 4px 12px rgba(16,185,129,0.6)' : 'none',
                                            }}
                                        />
                                    </div>
                                );
                            })}
                        </div>

                        {/* Actions */}
                        <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end' }}>
                            <button
                                onClick={() => { setClaimAction(null); setClaimSelectedIndices([]); }}
                                style={{ ...btnBase, backgroundColor: '#4b5563', padding: '0.75rem 1.5rem' }}
                            >
                                Cancel
                            </button>
                            <button
                                onClick={handleClaimOk}
                                disabled={!claimReady}
                                style={{
                                    ...btnBase,
                                    padding: '0.75rem 1.5rem',
                                    backgroundColor: claimReady ? '#10b981' : '#374151',
                                    cursor: claimReady ? 'pointer' : 'not-allowed',
                                    opacity: claimReady ? 1 : 0.5,
                                }}
                            >
                                OK
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default MahjongTable;

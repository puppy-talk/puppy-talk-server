"""
Simple in-memory cache for AI service
"""

import time
import hashlib
from typing import Dict, Any, Optional, Tuple
from threading import RLock


class SimpleCache:
    """Thread-safe in-memory cache with TTL support"""
    
    def __init__(self, default_ttl: int = 300):  # 5 minutes default
        self.default_ttl = default_ttl
        self._cache: Dict[str, Tuple[Any, float]] = {}
        self._lock = RLock()
    
    def _is_expired(self, expiry_time: float) -> bool:
        """Check if cache entry is expired"""
        return time.time() > expiry_time
    
    def _generate_key(self, *args, **kwargs) -> str:
        """Generate cache key from arguments"""
        content = str(args) + str(sorted(kwargs.items()))
        return hashlib.md5(content.encode()).hexdigest()
    
    def get(self, key: str) -> Optional[Any]:
        """Get value from cache"""
        with self._lock:
            if key in self._cache:
                value, expiry_time = self._cache[key]
                if not self._is_expired(expiry_time):
                    return value
                else:
                    # Remove expired entry
                    del self._cache[key]
            return None
    
    def set(self, key: str, value: Any, ttl: Optional[int] = None) -> None:
        """Set value in cache"""
        ttl = ttl or self.default_ttl
        expiry_time = time.time() + ttl
        
        with self._lock:
            self._cache[key] = (value, expiry_time)
    
    def delete(self, key: str) -> bool:
        """Delete key from cache"""
        with self._lock:
            if key in self._cache:
                del self._cache[key]
                return True
            return False
    
    def clear(self) -> None:
        """Clear all cache entries"""
        with self._lock:
            self._cache.clear()
    
    def cleanup_expired(self) -> int:
        """Remove expired entries and return count"""
        current_time = time.time()
        expired_keys = []
        
        with self._lock:
            for key, (_, expiry_time) in self._cache.items():
                if current_time > expiry_time:
                    expired_keys.append(key)
            
            for key in expired_keys:
                del self._cache[key]
        
        return len(expired_keys)
    
    def size(self) -> int:
        """Get cache size"""
        with self._lock:
            return len(self._cache)
    
    def cache_key_for_persona(
        self, 
        pet_name: str, 
        pet_persona: str, 
        personality_traits: list
    ) -> str:
        """Generate cache key for persona-based prompts"""
        return self._generate_key(
            pet_name=pet_name,
            pet_persona=pet_persona,
            personality_traits=sorted(personality_traits)
        )


# Global cache instance
_prompt_cache = SimpleCache(default_ttl=1800)  # 30 minutes for prompts


def get_prompt_cache() -> SimpleCache:
    """Get global prompt cache instance"""
    return _prompt_cache